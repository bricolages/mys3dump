package org.bricolages.mys3dump;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
class S3RowWriterFactory implements RowWriterFactory {
    private final Preprocessor preprocessor;
    private final RowFormatter formatter;
    private final S3OutputLocation outputLocation;
    private final int preferredObjectSize;
    private final boolean compress;

    private volatile int index = 0;

    public S3RowWriterFactory(Preprocessor preprocessor, RowFormatter formatter, S3OutputLocation outputLocation, int preferredObjectSize, boolean compress, boolean deleteObjects) {
        this.preprocessor = preprocessor;
        this.formatter = formatter;
        this.outputLocation = outputLocation;
        this.preferredObjectSize = preferredObjectSize;
        this.compress = compress;
        checkOutputLocation(deleteObjects);
    }

    @Override
    public S3RowWriter newRowWriter() throws IOException {
        return new S3RowWriter(new S3OutputStream(outputLocation.getBucket(), newKey(), compress), preprocessor, formatter, compress, preferredObjectSize);
    }

    synchronized String newKey() {
        String key = outputLocation.getPrefix() + String.format("%1$05d", index) + "." + formatter.getFormat();
        if (compress) key = key + ".gz";
        index++;
        return key;
    }

    void checkOutputLocation(boolean deleteObjects) {
        if (deleteObjects) this.outputLocation.deleteIfExist();
        try {
            this.outputLocation.errorIfExist();
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }
    }
}
