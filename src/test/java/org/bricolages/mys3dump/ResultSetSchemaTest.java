package org.bricolages.mys3dump;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultSetSchemaTest {

    @Mock
    ResultSetMetaData metadata;

    class Column {
        final String   name;
        final int      type;
        final String   typeName;

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

    @BeforeEach
    void init() throws Exception {
        when(metadata.getColumnCount()).thenReturn(cols.size());
        int i = 0;
        for(Column c : cols) {
            i++;
            when(metadata.getColumnName(i)).thenReturn(c.name);
            when(metadata.getColumnType(i)).thenReturn(c.type);
            when(metadata.getColumnTypeName(i)).thenReturn(c.typeName);
        }
    }

    @Test
    void newInstance() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        assertEquals(schema.getColumnCount(), 2);
    }

    @Test
    void getColumns() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        assertTrue(schema.getColumns().get(0) instanceof ResultSetColumn);
    }

    @Test
    void getColumnTypes() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        int i = 0;
        for(int t : schema.getColumnTypes()) {
            assertEquals(t, cols.get(i).type);
            i++;
        }
    }

    @Test
    void getColumnNames() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        int i = 0;
        for(String t : schema.getColumnNames()) {
            assertEquals(t, cols.get(i).name);
            i++;
        }
    }
}