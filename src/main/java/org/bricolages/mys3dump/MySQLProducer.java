package org.bricolages.mys3dump;

import java.sql.*;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shimpei-kodama on 2016/02/10.
 */
@Slf4j
class MySQLProducer {
    private final BlockingQueue<char[][]> queue;
    private final MySQLDataSource myds;

    public MySQLProducer(BlockingQueue<char[][]> queue, MySQLDataSource myds) {
        this.queue = queue;
        this.myds = myds;
    }

    WorkerResult execute(String query) {
        WorkerResult res = new WorkerResult(Thread.currentThread().getName());
        try {
            Connection conn = myds.newConnection();
            try (Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)) {
                stmt.setFetchSize(Integer.MIN_VALUE);
                logger.info("Execute query: " + query);
                try (ResultSet rs = stmt.executeQuery(query)) {
                    logger.info("Query returned");
                    res.addProcessedRowCount(produceRows(rs));
                }
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        res.finish();
        return res;
    }

    Long produceRows(ResultSet rs) throws SQLException {
        Long count = 0L;
        try {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                queue.put(newRow(rs, columnCount));
                count++;
                if (count % 10000000 == 0) {
                    logger.info("Rows read: " + count);
                    logger.info("Queue Size: " + queue.size() + ". Remaining capacity: " + queue.remainingCapacity());
                }
                if (Thread.currentThread().isInterrupted())
                    exitAvoidHang(Thread.currentThread().getName() + " interrupted.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            exitAvoidHang(e.getMessage());
        }
        return count;
    }

    void exitAvoidHang(String msg) {
        logger.error("Exit to avoid hand on ResultSet#close(): " + msg);
        System.exit(1);
    }

    char[][] newRow(ResultSet rs, int columnCount) throws SQLException {
        char[][] row = new char[columnCount][];
        for (int i = 0; i < columnCount; i++) {
            try {
                row[i] = nullToChar(rs.getString(i + 1)).toCharArray();
            } catch (SQLException e) {
                if (e.getErrorCode() == 0 && "S1009".equals(e.getSQLState())) {
                    logger.debug(e.getMessage() + ". Value dumped as \"0000-00-00 00:00:00\"");
                    row[i] = "0000-00-00 00:00:00".toCharArray();
                    continue;
                }
                throw e;
            }
        }
        return row;
    }

    String nullToChar(String s) {
        if (s == null) return "\0";
        return s;
    }
}
