package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stjepano.filestore.client.Bucket;
import com.stjepano.filestore.client.FileStore;
import com.stjepano.filestore.client.FileStoreException;
import com.stjepano.filestore.client.FileStoreServerException;
import com.stjepano.filestore.common.ErrorResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FileStore} that uses apache HttpClient
 */
public class OkHttpFileStore implements FileStore {

    private final URI serverUri;

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    public OkHttpFileStore(URI serverUri, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        this.serverUri = serverUri;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Bucket> getBuckets() {
        try {
            final Request request = new Request.Builder()
                    .url(serverUri.toURL())
                    .build();

            final Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful() && !response.isRedirect()) {
                final ErrorResponse errorResponse = Utils.errorResponse(response, this.objectMapper);
                throw new FileStoreServerException(response.code(), errorResponse.getMessage());
            }

            List<String> bucketNameList = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return bucketNameList.stream().map(this::newBucket).collect(Collectors.toList());

        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    @Override
    public Optional<Bucket> getBucket(String bucketName) {
        List<Bucket> buckets = getBuckets();
        return buckets.stream().filter( bucket -> bucketName.equals(bucket.getName()) ).findFirst();
    }

    @Override
    public Bucket createBucket(String bucketName) {
        try {
            final Request request = new Request.Builder()
                    .url(serverUri.toURL())
                    .post(RequestBody.create(MediaType.parse("text/plain"), bucketName))
                    .build();

            final Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful() && !response.isRedirect()) {
                final ErrorResponse errorResponse = Utils.errorResponse(response, this.objectMapper);
                throw new FileStoreServerException(response.code(), errorResponse.getMessage());
            }

            return new OkHttpBucket(this, bucketName);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    /*@Override
    public void deleteBucket(Bucket bucket) {
        try {
            final Request request = new Request.Builder()
                    .url(serverUri.resolve(bucket.getName()).toURL())
                    .delete()
                    .build();

            final Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful() && !response.isRedirect()) {
                final ErrorResponse errorResponse = Utils.from(response, objectMapper);
                throw new FileStoreServerException(response.code(), errorResponse.getMessage());
            }
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }*/

    public URI getServerUri() {
        return serverUri;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public OkHttpBucket newBucket(String bucketName) {
        return new OkHttpBucket(this, bucketName);
    }
}
