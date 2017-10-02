package com.stjepano.filestore.service;

/**
 * Thrown if bucket does not exist ...
 */
public class BucketDoesNotExistException extends ObjectStoreException {

    public BucketDoesNotExistException(BucketId bucketId) {
        super(String.format("Bucket '%s' does not exist!", bucketId));
    }

}
