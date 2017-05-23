package org.bricolages.mys3dump;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by shimpei-kodama on 2016/02/29.
 */
class ResultSetSchema {
    public final String[] columnNames;
    public final int[] columnTypes;
    public final int columnCount;

    public ResultSetSchema(ResultSetMetaData metadata) throws SQLException {
        int cc = metadata.getColumnCount();
        columnCount = cc;
        columnNames = new String[cc];
        columnTypes = new int[cc];
        for (int i = 0; i < cc; i++) {
            columnNames[i] = metadata.getColumnName(i + 1);
            columnTypes[i] = metadata.getColumnType(i + 1);
        }
    }
}
