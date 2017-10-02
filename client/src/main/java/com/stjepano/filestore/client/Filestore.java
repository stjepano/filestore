package com.stjepano.filestore.client;

import java.util.List;
import java.util.Optional;

/**
 * Represents a filestore server.
 *
 * Allows access to buckets.
 */
public interface Filestore {

    /**
     * Get a list of all buckets.
     * @return a list, never null
     */
    List<Bucket> getBuckets();

    /**
     * Get bucket specified by bucket name.
     * @param bucketName a bucket name
     * @return a {@link Bucket} object wrapped in {@link Optional}, empty Optional if bucket does not exist
     */
    Optional<Bucket> getBucket(String bucketName);

    /**
     * Create a bucket with specified name and return handle to it.
     * @param bucketName a bucket name
     * @return handle to bucket
     */
    Bucket createBucket(String bucketName);

    /**
     * Delete a bucket.
     *
     * @param bucket handle to bucket
     */
    void deleteBucket(Bucket bucket);

}
