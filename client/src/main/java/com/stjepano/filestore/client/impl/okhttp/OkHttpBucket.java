package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stjepano.filestore.client.Bucket;
import com.stjepano.filestore.client.FileStoreException;
import com.stjepano.filestore.client.FileStoreServerException;
import com.stjepano.filestore.common.ErrorResponse;
import com.stjepano.filestore.common.FileInfo;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link Bucket} that uses apache HttpClient.
 */
public class OkHttpBucket implements Bucket {

    private final OkHttpFileStore okHttpFileStore;
    private final String name;

    public OkHttpBucket(OkHttpFileStore okHttpFileStore, String name) {
        this.okHttpFileStore = okHttpFileStore;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    private void throwIfResponseInError(Response response) throws IOException {
        if (!response.isSuccessful() && !response.isRedirect()) {
            final ErrorResponse errorResponse = Utils.errorResponse(response, this.okHttpFileStore.getObjectMapper());
            throw new FileStoreServerException(response.code(), errorResponse.getMessage());
        }
    }

    private URI bucketUri() {
        return this.okHttpFileStore.getServerUri().resolve("/store/" + this.name + "/");
    }

    private URI fileUri(String filename) {
        return this.okHttpFileStore.getServerUri().resolve("/store/" + this.name + "/" + filename);
    }

    private OkHttpClient okHttpClient() {
        return this.okHttpFileStore.getOkHttpClient();
    }

    private ObjectMapper objectMapper() {
        return this.okHttpFileStore.getObjectMapper();
    }

    @Override
    public List<FileInfo> getFiles() {
        try {
            final Request request = new Request.Builder()
                    .url(bucketUri().toURL())
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);

            return objectMapper().readValue(
                    response.body().string(),
                    objectMapper().getTypeFactory().constructCollectionType(List.class, FileInfo.class)
            );

        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    @Override
    public boolean fileExists(String filename) {
        return getFileInfo(filename).isPresent();
    }

    @Override
    public Optional<FileInfo> getFileInfo(String filename) {
        List<FileInfo> fileInfos = getFiles();
        return fileInfos.stream().filter(fileInfo -> fileInfo.getName().equals(filename)).findFirst();
    }

    @Override
    public void uploadFile(Path sourceFilePath) {
        uploadFile(sourceFilePath, null);
    }

    @Override
    public void uploadFile(Path sourceFilePath, String newFilename) {
        try {
            if (!Files.exists(sourceFilePath)
                    || !Files.isRegularFile(sourceFilePath)
                    || !Files.isReadable(sourceFilePath)) {
                throw new FileStoreException(String.format(
                        "File '%s' does not exist, is not a regular file or can not be read!",
                        sourceFilePath.normalize().toString()
                ));
            }
            RequestBody requestBody = createUploadRequest(sourceFilePath);
            URI bucketUri = bucketUri();
            URL targetUrl = bucketUri.toURL();
            if (newFilename != null) {
                HttpUrl httpUrl = HttpUrl.get(bucketUri);
                if (httpUrl == null) {
                    throw new FileStoreException("Could not build URL from " + bucketUri.toString());
                }
                targetUrl = httpUrl.newBuilder().addQueryParameter("filename", newFilename).build().url();
            }

            final Request request = new Request.Builder()
                    .url(targetUrl)
                    .post(requestBody)
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    @Override
    public void overwriteFile(Path sourceFilePath, String filename) {
        try {
            if (!Files.exists(sourceFilePath)
                    || !Files.isRegularFile(sourceFilePath)
                    || !Files.isReadable(sourceFilePath)) {
                throw new FileStoreException(String.format(
                        "File '%s' does not exist, is not a regular file or can not be read!",
                        sourceFilePath.normalize().toString()
                ));
            }
            RequestBody requestBody = createUploadRequest(sourceFilePath);
            final Request request = new Request.Builder()
                    .url(fileUri(filename).toURL())
                    .put(requestBody)
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    private RequestBody createUploadRequest(Path sourceFilePath) throws IOException {
        return new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                sourceFilePath.getFileName().toString(),
                                RequestBody.create(MediaType.parse("application/octet-stream"), Files.readAllBytes(sourceFilePath))
                        )
                        .build();
    }

    @Override
    public void downloadFile(String filename, Path targetFile) {
        try {
            final Request request = new Request.Builder()
                    .url(fileUri(filename).toURL())
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);

            if (response.body() == null) {
                throw new FileStoreException("Could not download file as server returned null body.");
            }
            byte[] bytes = response.body().bytes();
            if (bytes == null) {
                throw new FileStoreException("Could not download file as server returned null bytes.");
            }
            Files.write(targetFile, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            final Request request = new Request.Builder()
                    .url(fileUri(filename).toURL())
                    .delete()
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }

    @Override
    public void deleteBucketAndAllFiles() {
        try {
            final Request request = new Request.Builder()
                    .url(bucketUri().toURL())
                    .delete()
                    .build();

            final Response response = okHttpClient().newCall(request).execute();
            throwIfResponseInError(response);
        } catch (IOException e) {
            throw new FileStoreException(e);
        }
    }
}
