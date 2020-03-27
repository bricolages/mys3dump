package org.bricolages.mys3dump;

import org.junit.jupiter.api.Test;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetColumnTest {

    @Test
    void sqlExpression_geometry() {
        String colName = "geom";
        ResultSetColumn c = new ResultSetColumn(colName, Types.BLOB, "GEOMETRY");
        assertEquals(String.format("ST_AsText(%s) as %s", colName, colName), c.sqlExpression());
    }

    @Test
    void sqlExpression_default() {
        String columnName = "col";
        ResultSetColumn c = new ResultSetColumn(columnName, Types.BLOB, "INTEGER");
        assertEquals(columnName, c.sqlExpression());
    }
}