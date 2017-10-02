package com.stjepano.filestore.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * File service responsible for reading and storing files.
 */
@Service
public interface FileService {

    /**
     * Return a list of bucket names, sorted alphabetically
     * @return a list of strings
     */
    List<String> getBuckets();

    /**
     * Create new bucket.
     * @param bucketId the id of the bucket
     * @throws BucketAlreadyExistsException if bucket already exists
     * @throws InvalidBucketIdException if bucket name is invalid
     */
    void createBucket(BucketId bucketId) throws BucketAlreadyExistsException;

    /**
     * Delete bucket and all files in it
     * @param bucketId the id of the bucket
     * @throws BucketDoesNotExistException if bucket does not exist
     */
    void deleteBucket(BucketId bucketId) throws BucketDoesNotExistException;

    /**
     * Get all files sorted alphabetically in ascending order
     * @param bucketId the id of the bucket
     * @return a list of {@link FileInfo} objects
     * @throws BucketDoesNotExistException if bucket does not exist
     */
    List<FileInfo> getFiles(BucketId bucketId) throws BucketDoesNotExistException;

    /**
     * Delete file from bucket.
     * @param fileId the id of the file
     * @throws FileDoesNotExistException if file does not exist
     * @throws BucketDoesNotExistException if bucket does not exist
     */
    void deleteFile(FileId fileId) throws BucketDoesNotExistException, FileDoesNotExistException;

    /**
     * Upload file to bucket
     *
     * @param fileId the id of the file
     * @param dataStream file data stream
     * @throws BucketDoesNotExistException if bucket does not exist
     * @throws FileAlreadyExistException if file already exists
     */
    void upload(FileId fileId, InputStream dataStream) throws BucketDoesNotExistException, FileAlreadyExistException;

    /**
     * Upload file to bucket and overwrite file that already exist.
     *
     * @param fileId the id of the file
     * @param dataStream HTTP file data stream
     * @throws BucketDoesNotExistException if bucket does not exist
     * @throws FileDoesNotExistException if file specified with filename does not exist
     */
    void overwrite(FileId fileId, InputStream dataStream) throws BucketDoesNotExistException, FileDoesNotExistException;

    /**
     * Download file from bucket as {@link Resource}.
     *
     * @param fileId the id of the file
     * @return spring resource
     * @throws BucketDoesNotExistException if bucket does not exist
     * @throws FileDoesNotExistException if file does not exist
     */
    Resource download(FileId fileId) throws BucketDoesNotExistException, FileDoesNotExistException;
}
