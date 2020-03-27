package org.bricolages.mys3dump;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shimpei-kodama on 2016/02/18.
 */
class MySQLDataSource {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final String connectionString;
    private final String username;
    private final String password;

    public MySQLDataSource(String host, int port, String db, String username, String password, String property) throws ClassNotFoundException {
        this.connectionString = "jdbc:mysql://" + host + ":" + port + "/" + db + "?" + property;
        this.username = username;
        this.password = password;
        Class.forName("com.mysql.jdbc.Driver");
    }

    Connection newConnection() throws SQLException {
        logger.info("Connecting to: " + this.connectionString);
        return DriverManager.getConnection(connectionString, username, password);
    }

    <T> List<List<T>> execute(String sql, Class<T> cls) throws SQLException {
        List<List<T>> rows = new ArrayList<>();
        logger.info("Execute query: " + sql);
        try (Connection c = newConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            logger.info("Query returned: " + sql);
            while (rs.next()) {
                List<T> r = new ArrayList<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    r.add(rs.getObject(i) == null ? null : rs.getObject(i, cls));
                }
                rows.add(r);
            }
        }
        return rows;
    }

    ResultSetMetaData getQueryMetadata(ScanQuery query) throws SQLException {
        String sql = query + " LIMIT 1";
        logger.info("Execute query for metadata: " + sql);
        try (ResultSet rs = newConnection().createStatement().executeQuery(sql)) {
            logger.info("Query returned: " + sql);
            return rs.getMetaData();
        }
    }

    ResultSetSchema getTableSchema(String table) throws SQLException {
        DatabaseMetaData meta = newConnection().getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, "%")) {
            List<ResultSetColumn> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(new ResultSetColumn(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"), rs.getString("TYPE_NAME")));
            }
            return new ResultSetSchema(columns);
        }
    }

    String getConnectionString() {
        return connectionString;
    }
}
