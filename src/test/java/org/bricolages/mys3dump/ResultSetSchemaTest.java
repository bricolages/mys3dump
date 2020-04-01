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

class ResultSetSchemaTest {

    ResultSetMetaData metadata;

    @BeforeEach
    void init() throws Exception {
        metadata = (new ResultSetMetaDataMock()).make();
    }

    @Test
    void newInstance() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        assertEquals(schema.getColumnCount(), 2);
    }

    @Test
    void getColumns() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        assertTrue(schema.getColumns().get(0) != null);
    }

    @Test
    void getColumnTypes() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        int i = 1;
        for(int t : schema.getColumnTypes()) {
            assertEquals(t, metadata.getColumnType(i));
            i++;
        }
    }

    @Test
    void getColumnNames() throws SQLException {
        ResultSetSchema schema = ResultSetSchema.newInstance(metadata);
        int i = 1;
        for(String t : schema.getColumnNames()) {
            assertEquals(t, metadata.getColumnName(i));
            i++;
        }
    }
}