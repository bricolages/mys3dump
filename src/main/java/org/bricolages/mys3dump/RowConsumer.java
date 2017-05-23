package org.bricolages.mys3dump;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by shimpei-kodama on 2016/02/08.
 */
class RowConsumer {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final BlockingQueue<char[][]> queue;
    private final RowWriterFactory writerFactory;

    public RowConsumer(BlockingQueue<char[][]> queue, RowWriterFactory writerFactory) {
        this.queue = queue;
        this.writerFactory = writerFactory;
    }

    WorkerResult execute() {
        WorkerResult res = new WorkerResult(Thread.currentThread().getName());
        try {
            char[][] row;
            masterLoop:
            while (true) {
                try (RowWriter writer = writerFactory.newRowWriter()) {
                    while (!writer.shouldRotate()) {
                        row = queue.take();
                        if (Thread.currentThread().isInterrupted()) break masterLoop;
                        if (isRowEOF(row)) {
                            res.addProcessedRowCount(writer.getRowCount());
                            break masterLoop;
                        }
                        writer.writeRow(row);
                    }
                    res.addProcessedRowCount(writer.getRowCount());
                }
            }
            res.finish();
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean isRowEOF(char[][] row) {
        return row.length == 0;
    }
}
