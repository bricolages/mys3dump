package org.bricolages.mys3dump;

import java.nio.CharBuffer;
import java.sql.Types;
import java.util.List;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
class JSONRowFormatter implements RowFormatter {
    private final List<String> columnNames;
    private final List<Integer> columnTypes;
    private final int columnCount;

    public JSONRowFormatter(ResultSetSchema rsSchema) {
        this.columnNames = rsSchema.getColumnNames();
        this.columnTypes = rsSchema.getColumnTypes();
        this.columnCount = rsSchema.getColumnCount();
    }

    @Override
    public String format(char[][] row) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String d = "";
        for (int i = 0; i < columnCount; i++) {
            if (isRowNull(row[i])) continue;
            sb.append(d);
            sb.append('"');
            sb.append(columnNames.get(i));
            sb.append("\":");
            sb.append(formatValue(row[i], columnTypes.get(i)));
            d = ",";
        }
        sb.append("}\n");
        return sb.toString();
    }

    char[] formatValue(char[] value, int columnType) {
        // expect value is not null
        switch (columnType) {
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.ARRAY:
                return value;
            default:
                return escape(value);
        }
    }

    boolean isRowNull(char[] cs) {
        return (cs.length > 0 && cs[0] == '\0');
    }

    @Override
    public String getFormat() {
        return "json";
    }

    CharBuffer expandCharBuffer(CharBuffer cb) {
        return CharBuffer.allocate(cb.capacity() + 1).put((CharBuffer)cb.flip());
    }

    CharBuffer shrinkCharBuffer(CharBuffer cb) {
        return CharBuffer.allocate(cb.capacity() - 1).put((CharBuffer)cb.flip());
    }

    char[] escape(char[] cs) {
        CharBuffer cb;
        cb = CharBuffer.allocate(cs.length + 2);
        cb.put('"');
        for (char c : cs) {
            switch (c) {
                case '\\':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('\\');
                    break;
                case '"':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('"');
                    break;
                case '/':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('/');
                    break;
                case '\n':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('n');
                    break;
                case '\r':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('r');
                    break;
                case '\b':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('b');
                    break;
                case '\t':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('t');
                    break;
                case '\f':
                    cb = expandCharBuffer(cb);
                    cb.put('\\');
                    cb.put('f');
                    break;
                case '\0':
                    cb = shrinkCharBuffer(cb);
                    break;
                default:
                    cb.put(c);
                    break;
            }
        }
        cb.put('"');
        return ((CharBuffer)cb.flip()).array();
    }
}
