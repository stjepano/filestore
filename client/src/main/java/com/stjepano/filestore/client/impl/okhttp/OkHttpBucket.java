package com.stjepano.filestore.client.impl.okhttp;

import com.stjepano.filestore.client.Bucket;
import com.stjepano.filestore.common.FileInfo;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of {@link Bucket} that uses apache HttpClient.
 */
public class OkHttpBucket implements Bucket {

    private final OkHttpClient okHttpClient;
    private final URI serverUri;
    private final String name;

    public OkHttpBucket(OkHttpClient okHttpClient, URI serverUri, String name) {
        this.okHttpClient = okHttpClient;
        this.serverUri = serverUri;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<FileInfo> getFiles() {
        return null;
    }

    @Override
    public boolean fileExists(String filename) {
        return false;
    }

    @Override
    public FileInfo getFileInfo(String filename) {
        return null;
    }

    @Override
    public void uploadFile(Path sourceFilePath) {

    }

    @Override
    public void uploadFile(Path sourceFilePath, String newFilename) {

    }

    @Override
    public void overwriteFile(Path sourceFilePath, String filename) {

    }

    @Override
    public void downloadFile(String filename, Path targetFile) {

    }

    @Override
    public void deleteFile(String filename) {

    }

    @Override
    public void delete() {

    }
}
