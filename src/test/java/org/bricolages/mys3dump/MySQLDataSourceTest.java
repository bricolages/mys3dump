package org.bricolages.mys3dump;

import org.bricolages.mys3dump.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySQLDataSourceTest {

    @Test
    void getTableSchema_nonexistentTable() throws SQLException, ClassNotFoundException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        when(meta.getColumns(any(),any(),any(),any())).thenReturn(rs);
        MySQLDataSource ds = new MySQLDataSource("", 0, "", "", "", "");
        MySQLDataSource spy_ds = spy(ds);
        doReturn(meta).when(spy_ds).getDatabaseMetaData();

        String table_name = "test_table";
        Exception ex = assertThrows(ApplicationException.class, () -> {
            spy_ds.getTableSchema(table_name);
        });
        assertEquals(ex.getMessage(), String.format("Table %s doesn't exist", table_name));
    }
}