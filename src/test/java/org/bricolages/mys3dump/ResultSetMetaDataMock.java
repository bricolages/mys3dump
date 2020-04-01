package org.bricolages.mys3dump;

import org.mockito.Mockito;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

class ResultSetMetaDataMock {

    class Column {
        final String name;
        final int type;
        final String typeName;

        Column(String name, int type, String typeName) {
            this.name = name;
            this.type = type;
            this.typeName = typeName;
        }
    }

    final List<Column> cols = Arrays.asList(
            new Column("c1", 1, "INTEGER"),
            new Column("c2", 2, "GEOMETRY")
    );

    public ResultSetMetaData make() throws SQLException {
        ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
        when(metadata.getColumnCount()).thenReturn(cols.size());
        int i = 1;
        for(ResultSetMetaDataMock.Column c : cols) {
            when(metadata.getColumnName(i)).thenReturn(c.name);
            when(metadata.getColumnType(i)).thenReturn(c.type);
            when(metadata.getColumnTypeName(i)).thenReturn(c.typeName);
            i++;
        }
        return metadata;
    }
}
