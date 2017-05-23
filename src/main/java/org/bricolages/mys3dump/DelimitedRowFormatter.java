package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
abstract class DelimitedRowFormatter implements RowFormatter {
    private final String delimiter;
    private final int columnCount;

    public DelimitedRowFormatter(String delimiter, int columnCount) {
        this.delimiter = delimiter;
        this.columnCount = columnCount;
    }

    @Override
    public String format(char[][] row) {
        StringBuilder sb = new StringBuilder();
        String d = "";
        for (int i = 1; i < columnCount; i++) {
            sb.append(d);
            sb.append(escape(row[i]));
            d = delimiter;
        }
        sb.append("\n");
        return sb.toString();
    }

    char[] escape(char[] cs) {
        if (cs.length > 0 && cs[0] == '\0') return NULL_STRING;
        return cs;
    }
}
