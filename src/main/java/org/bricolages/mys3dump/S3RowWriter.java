package org.bricolages.mys3dump;

import java.io.*;

/**
 * Created by shimpei-kodama on 2016/02/02.
 */
class S3RowWriter extends RowWriter {
    private final S3OutputStream out;

    public S3RowWriter(S3OutputStream out, Preprocessor preprocessor, RowFormatter formatter, boolean compress, int preferredObjectSize) throws IOException {
        super(out, preprocessor, formatter, compress, preferredObjectSize);
        this.out = out;
    }

    @Override
    void flushRow() throws IOException {
        super.flushRow();
        size = out.size();
    }
}