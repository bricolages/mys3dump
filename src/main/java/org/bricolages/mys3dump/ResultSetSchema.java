package org.bricolages.mys3dump;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shimpei-kodama on 2016/02/29.
 */
class ResultSetSchema {
    private final List<ResultSetColumn> columns;
    private final int columnCount;
    private final List<Integer> columnTypes;
    private final List<String> columnNames;

    public ResultSetSchema(List<ResultSetColumn> columns) throws SQLException {
        this.columns = columns;
        this.columnCount = columns.size();
        this.columnTypes = columns.stream().map(c -> c.type).collect(Collectors.toList());
        this.columnNames = columns.stream().map(c -> c.name).collect(Collectors.toList());
    }

    static public ResultSetSchema newInstance(ResultSetMetaData metadata) throws SQLException {
        List<ResultSetColumn> columns = new ArrayList<ResultSetColumn>();
        for (int i = 0; i < metadata.getColumnCount(); i++) {
            int idx = 1 + i;
            ResultSetColumn c = new ResultSetColumn(metadata.getColumnName(idx), metadata.getColumnType(idx), metadata.getColumnTypeName(idx));
            columns.add(c);
        }
        return new ResultSetSchema(columns);
    }

    public List<ResultSetColumn> getColumns() {
        return columns;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<Integer> getColumnTypes() {
        return columnTypes;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
