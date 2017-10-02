package com.stjepano.filestore.client;

import com.stjepano.filestore.common.FileInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * A filestore bucket.
 *
 * Allows access to files in the bucket.
 */
public interface Bucket {

    String getName();

    List<FileInfo> getFiles();

    boolean fileExists(String filename);

    FileInfo getFileInfo(String filename);

    void uploadFile(Path sourceFilePath);

    void uploadFile(Path sourceFilePath, String newFilename);

    void overwriteFile(Path sourceFilePath, String filename);

    void downloadFile(String filename, Path targetFile);
}
