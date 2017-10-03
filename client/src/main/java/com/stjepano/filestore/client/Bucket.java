package com.stjepano.filestore.client;

import com.stjepano.filestore.common.FileInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A filestore bucket.
 *
 * Allows access to files in the bucket.
 */
public interface Bucket {

    /**
     * Get the name of a bucket
     * @return string, never null
     */
    String getName();

    /**
     * Get all files in the bucket.
     * @return a list of {@link FileInfo} objects
     */
    List<FileInfo> getFiles();

    /**
     * Check if file exists
     * @param filename the name of the file
     * @return true if exists, false if not
     */
    boolean fileExists(String filename);

    /**
     * Get {@link FileInfo} object for given file
     * @param filename name of the file
     * @return a {@link FileInfo} object wrapped in {@link Optional} if file exists, empty optional if not
     */
    Optional<FileInfo> getFileInfo(String filename);

    /**
     * Upload a file to file store bucket from given path.
     *
     * File in the store will have same name as local file.
     *
     * @param sourceFilePath path of the file
     */
    void uploadFile(Path sourceFilePath);

    /**
     * Upload a file to file store bucket from given path.
     *
     * File in the store will have specified name
     *
     * @param sourceFilePath path of the file
     * @param newFilename desired filename in the store
     */
    void uploadFile(Path sourceFilePath, String newFilename);

    /**
     * Overwrite store file with local file
     * @param sourceFilePath the path to local file
     * @param filename name of the file in the store
     */
    void overwriteFile(Path sourceFilePath, String filename);

    /**
     * Download file to local path.
     * @param filename name of the file in the store
     * @param targetFile path of local file where file will be downloaded.
     */
    void downloadFile(String filename, Path targetFile);

    /**
     * Delete file in the store.
     * @param filename name of the file in the store
     */
    void deleteFile(String filename);

    /**
     * Delete this bucket and all files in it, use with care.
     */
    void delete();
}
