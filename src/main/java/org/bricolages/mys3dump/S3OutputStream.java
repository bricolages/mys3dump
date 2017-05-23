package org.bricolages.mys3dump;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by shimpei-kodama on 2016/02/08.
 */
class S3OutputStream extends ByteArrayOutputStream {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final int EMPTY_GZIPPED_FILESIZE = 20;
    private final int EMPTY_PLAIN_FILESIZE = 0;

    private final AmazonS3 client;
    private final String bucket;
    private final String key;
    private final boolean compress;
    private volatile boolean isClosed = false;

    public S3OutputStream(String bucket, String key, boolean compress) {
        this.bucket = bucket;
        this.key = key;
        this.compress = compress;
        this.client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
    }

    void upload() {
        ObjectMetadata metadata = new ObjectMetadata();
        if (compress) metadata.setContentEncoding("gzip");
        metadata.setContentLength(this.count);
        client.putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(this.toByteArray()), metadata));
        logger.info("S3 object created " + "(" + this.count + " bytes): " + bucket + "/" + key);
        logger.debug("Memory Usage:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        this.reset();
    }

    @Override
    public void close() throws IOException {
        if (isClosed) throw new IOException("Already closed.");
        isClosed = true;
        this.flush();
        upload();
        super.close();
    }
}
