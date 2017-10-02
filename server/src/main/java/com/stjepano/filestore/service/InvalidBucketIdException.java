package com.stjepano.filestore.service;

/**
 * Thrown if bucket name is invalid.
 */
public class InvalidBucketIdException extends IllegalArgumentException {

    public InvalidBucketIdException(String bucketName) {
        super(String.format("Bucket name '%s' is not valid!", bucketName == null ? "null" : bucketName));
    }

}
