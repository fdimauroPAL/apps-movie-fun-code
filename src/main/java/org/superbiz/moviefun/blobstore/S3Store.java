package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.*;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Optional;

public class S3Store implements BlobStore {
    AmazonS3Client s3Client;
    String s3BucketName;
    private final Tika tika = new Tika();

    public S3Store(AmazonS3Client s3Client, String s3BucketName) {
        this.s3Client = s3Client;
        this.s3BucketName = s3BucketName;

        if (!s3Client.doesBucketExist(s3BucketName)) {
            s3Client.createBucket(s3BucketName);
        }
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.getContentType());
        s3Client.putObject(s3BucketName, blob.getName(), blob.getInputStream(), objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

        if (!s3Client.doesObjectExist(s3BucketName, name)) {
            return Optional.empty();
        }

        try (S3Object s3Object = s3Client.getObject(s3BucketName, name)) {
            S3ObjectInputStream content = s3Object.getObjectContent();

            byte[] bytes = IOUtils.toByteArray(content);

            return Optional.of(new Blob(
                    name,
                    new ByteArrayInputStream(bytes),
                    tika.detect(bytes)
            ));
        }
    }

    @Override
    public void deleteAll() {

    }




}
