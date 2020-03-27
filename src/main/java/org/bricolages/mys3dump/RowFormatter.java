package org.bricolages.mys3dump;

import java.sql.SQLException;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
interface RowFormatter {
    char[] NULL_STRING = new char[]{'\\', 'N'};
    char[] EMPTY_STRING = new char[0];

    String format(char[][] row);

    String getFormat();

    static RowFormatter newInstance(String format, ResultSetSchema rsSchema) throws SQLException {
        if (format.equals("csv")) return new CSVRowFormatter(rsSchema.getColumnTypes().size());
        if (format.equals("tsv")) return new TSVRowFormatter(rsSchema.getColumnTypes().size());
        if (format.equals("json")) return new JSONRowFormatter(rsSchema);
        throw new IllegalArgumentException("\"" + format + "\" is not supported. Use csv, tsv or json.");
    }
}
