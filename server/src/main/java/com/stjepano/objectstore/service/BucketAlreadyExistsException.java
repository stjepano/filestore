package com.stjepano.objectstore.service;

/**
 * Thrown if bucket already exist.
 */
public class BucketAlreadyExistsException extends ObjectStoreException {

    public BucketAlreadyExistsException(BucketId bucketId) {
        super(String.format("Bucket '%s' already exists!", bucketId));
    }
}
