package org.bricolages.mys3dump;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by shimpei-kodama on 2016/03/02.
 */
abstract class RowWriter implements AutoCloseable, Flushable {
    private static final int ROW_BUFFER_SIZE = 128;

    protected final Writer writer;
    protected final Preprocessor preprocessor;
    protected final RowFormatter formatter;
    protected boolean compress;
    protected final int preferredSize;
    protected int size = 0;
    protected Long rowCount = 0L;
    protected StringBuilder sb = new StringBuilder();

    public RowWriter(OutputStream out, Preprocessor preprocessor, RowFormatter formatter, boolean compress, int preferredSize) throws IOException {
        this.preprocessor = preprocessor;
        this.formatter = formatter;
        this.compress = compress;
        this.preferredSize = preferredSize;
        if (compress) out = new GZIPOutputStream(out);
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        flushRow();
        writer.flush();
    }

    void flushRow() throws IOException {
        writer.write(sb.toString());
        sb = new StringBuilder();
    }

    void writeRow(char[][] row) throws IOException {
        sb.append(formatter.format(preprocessor.process(row)));
        rowCount++;
        if (rowCount % ROW_BUFFER_SIZE == 0) flushRow();
    }

    Long getRowCount() {
        return rowCount;
    }

    boolean shouldRotate() {
        return size > preferredSize;
    }
}
