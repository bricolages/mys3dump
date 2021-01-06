package org.bricolages.mys3dump;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shimpei-kodama on 2016/02/08.
 */
class RowConsumer {
    static private final Logger logger = LoggerFactory.getLogger(RowConsumer.class);

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
