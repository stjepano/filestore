package com.stjepano.filestore.client.impl.okhttp;

import com.stjepano.filestore.client.Bucket;
import com.stjepano.filestore.common.FileInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of {@link Bucket} that uses apache HttpClient.
 */
public class OkHttpBucket implements Bucket {

    private final String name;

    public OkHttpBucket(String name) {
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
}
