package org.bricolages.mys3dump;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shimpei-kodama on 2016/03/17.
 */
@Slf4j
class S3OutputLocation {
    private final String bucket;
    private final String prefix;
    private final String delimiter;
    private final AmazonS3 client;

    public S3OutputLocation(String bucket, String prefix, String delimiter) {
        this.bucket = bucket.replaceAll("/", "");
        this.prefix = prefix.replaceFirst("^/", "").replaceAll("//", "/");
        this.delimiter = delimiter;
        this.client = AmazonS3ClientBuilder.defaultClient();
    }

    void deleteIfExist() {
        if (notExists()) return;
        ObjectListing objects = listObjects(1000); // Retrieve 1000 object info at once
        while (objects.getObjectSummaries().size() > 0)
        do {
            List<String> keys = objects.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
            for (String key : keys) {
                logger.info("Delete object: " + bucket + "/" + key);
            }
            deleteObjects(keys);
        } while (!isListEmpty(objects = listNextObjects(objects)));
    }

    void deleteObjects(List<String> keys) {
        client.deleteObjects(new DeleteObjectsRequest(bucket).withKeys(keys.toArray(new String[keys.size()])));
    }

    void errorIfExist() throws FileAlreadyExistsException {
        if (notExists()) return;
        throw new FileAlreadyExistsException(bucket + "/" + prefix + " already exists.");
    }

    boolean notExists() {
        return isListEmpty(listObjects(1));
    }

    boolean isListEmpty(ObjectListing list) {
        return list.getObjectSummaries().isEmpty();
    }

    ObjectListing listObjects(Integer maxKey) {
        return client.listObjects(new ListObjectsRequest().
                withBucketName(bucket).
                withPrefix(prefix).
                withDelimiter(delimiter). // Prevent unintended deletion of objects in differenct hierarchy
                withMaxKeys(maxKey));
    }

    ObjectListing listNextObjects(ObjectListing list) {
        return client.listNextBatchOfObjects(list);
    }

    String getBucket() {
        return bucket;
    }

    String getPrefix() {
        return prefix;
    }
}
