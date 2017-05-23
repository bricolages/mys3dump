package org.bricolages.mys3dump;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MyS3Dump {
    static private final Logger logger = Logger.getLogger(MyS3Dump.class);

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException, ExecutionException, InterruptedException {
        Parameters params = new Parameters(args);
        MySQLDataSource myds = new MySQLDataSource(params.getHost(), params.getPort(), params.getDatabase(), params.getUsername(), params.getPassword(), params.getConnectionProperty());
        try {
            ScanQueryBuilder builder = new ScanQueryBuilder(params.getTable()).setQuery(params.getQuery())
                    .setPartitionInfo(myds, params.getPartitionColumn(), params.getPartitionNumber());
            List<ScanQuery> queries = builder.getScanQueries();
            ResultSetSchema resultSetSchema = new ResultSetSchema(myds.getMetadata(queries.get(0)));
            Preprocessor preprocessor = new Preprocessor(resultSetSchema, new TimeZonePreprocessOperation(params.getSrcZoneOffset(), params.getDstZoneOffset()));
            RowFormatter rowFormatter = RowFormatter.newInstance(params.getFormat(), resultSetSchema);
            RowWriterFactory rowWriterFactory = new S3RowWriterFactory(preprocessor, rowFormatter, new S3OutputLocation(params.getBucket(), params.getPrefix(), params.getObjectKeyDelimiter())
                    , params.getObjectSize(), params.getCompress(), params.getDeleteObject());
            logger.info("Start Dump.");
            new MyS3Dump(myds, queries, rowWriterFactory, params.getWriteConcurrency()).dump();
            logger.info("Dump finished.");
        } catch (EmptyTableException e) {
            logger.warn(e.getMessage());
        }
    }

    private final int QUEUE_SIZE = 2048;
    private final MySQLDataSource myds;
    private final List<ScanQuery> queries;
    private final RowWriterFactory rowWriterFactory;
    private final int writeConcurrency;

    private MyS3Dump(MySQLDataSource myds, List<ScanQuery> queries, RowWriterFactory rowWriterFactory, int writeConcurrency) {
        this.myds = myds;
        this.queries = queries;
        this.rowWriterFactory = rowWriterFactory;
        this.writeConcurrency = writeConcurrency;
    }

    private void dump() throws SQLException, InterruptedException, ExecutionException {
        BlockingQueue<char[][]> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        logger.info("Reader thread number: " + this.queries.size());
        ExecutorService producerPool = Executors.newFixedThreadPool(queries.size() * 2);
        try {
            logger.info("Writer thread number: " + writeConcurrency);
            ExecutorService consumerPool = Executors.newFixedThreadPool(writeConcurrency * 2);
            try {
                List<CompletableFuture<WorkerResult>> consumers = getConsumers(queue, consumerPool);
                List<CompletableFuture<WorkerResult>> producers = getProducers(queue, producerPool);
                waitFirstCompletion(producers, consumers);
                List<WorkerResult> producers_result = getResults(producers);
                logger.info("Total read rows: " + totalRowCount(producers_result));
                addEOFSignal(queue, writeConcurrency);
                List<WorkerResult> consumers_result = getResults(consumers);
                logger.info("Total write rows: " + totalRowCount(consumers_result));
            } catch (Exception e) {
                // Print StackTrace before System#exit() called by MySQLProducer in pool shutting down procedure.
                // This may cause printing same stacktrace twice,  in some case.
                e.printStackTrace();
                throw e;
            } finally {
                addEOFSignal(queue, writeConcurrency);
                shutdownThreadPool(consumerPool);
            }
        } finally {
            shutdownThreadPool(producerPool);
        }
    }


    private List<CompletableFuture<WorkerResult>> getConsumers(BlockingQueue<char[][]> queue, ExecutorService consumerPool) {
        RowConsumer rowConsumer = new RowConsumer(queue, rowWriterFactory);
        return IntStream.range(0, writeConcurrency)
                .mapToObj(i -> i + 1)
                .map(e -> CompletableFuture.supplyAsync(rowConsumer::execute, consumerPool))
                .collect(Collectors.<CompletableFuture<WorkerResult>>toList());
    }

    private List<CompletableFuture<WorkerResult>> getProducers(BlockingQueue<char[][]> queue, ExecutorService producerPool) {
        MySQLProducer myProducer = new MySQLProducer(queue, myds);
        return queries.stream()
                .map(q -> CompletableFuture.supplyAsync(() -> myProducer.execute(q.toString()), producerPool))
                .collect(Collectors.<CompletableFuture<WorkerResult>>toList());
    }

    private void addEOFSignal(BlockingQueue<char[][]> queue, int num) {
        IntStream.range(0, num).forEach(dummy -> queue.add(new char[0][0]));
    }

    private Long totalRowCount(List<WorkerResult> results) throws InterruptedException, ExecutionException {
        return results.stream()
                .map(WorkerResult::getProcessedRowCount)
                .collect(Collectors.summingLong(f -> f));
    }

    private void waitFirstCompletion(List<CompletableFuture<WorkerResult>> producers, List<CompletableFuture<WorkerResult>> consumers) throws InterruptedException, ExecutionException {
        List<CompletableFuture<WorkerResult>> list = new ArrayList<>(producers);
        list.addAll(consumers);
        getCompletedFuture(list);
    }

    private List<WorkerResult> getResults(List<CompletableFuture<WorkerResult>> futures) throws InterruptedException, ExecutionException {
        List<CompletableFuture<WorkerResult>> fs = new ArrayList<>(futures);
        List<WorkerResult> results = new ArrayList<>();
        while (fs.size() > 0) {
            CompletableFuture<WorkerResult> f = getCompletedFuture(fs);
            results.add(f.getNow(null));
            fs.remove(f);
        }
        return results;
    }

    private CompletableFuture<WorkerResult> getCompletedFuture(List<CompletableFuture<WorkerResult>> futures) throws InterruptedException, ExecutionException {
        int waitMs = 2500 / futures.size();
        while (true) {
            for (CompletableFuture<WorkerResult> f : futures) {
                try {
                    if(f.get(waitMs, TimeUnit.MILLISECONDS) != null) return f;
                } catch (TimeoutException e) {
                    //Do Nothing
                }
            }
        }
    }

    private void shutdownThreadPool(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (isThreadPoolShutdown(pool)) return;
        pool.shutdownNow();
        logger.warn("Forcibly shutting down thread pool: " + pool.toString());
        if (isThreadPoolShutdown(pool)) return;
        String msg = "Unable to shutdown thread pool: " + pool.toString();
        logger.error(msg);
        throw new RuntimeException(msg);
    }

    boolean isThreadPoolShutdown(ExecutorService pool) throws InterruptedException {
        int SHUTDOWN_WAIT_MS = 30000;
        return pool.awaitTermination(SHUTDOWN_WAIT_MS, TimeUnit.MILLISECONDS);
    }
}
