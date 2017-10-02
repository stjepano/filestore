package com.stjepano.filestore.service.filesystem;

/**
 * Thrown if trying to access file outside of bucket.
 */
public class BucketAccessViolationException extends RuntimeException {

    public BucketAccessViolationException() {
    }

    public BucketAccessViolationException(String message) {
        super(message);
    }

    public BucketAccessViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BucketAccessViolationException(Throwable cause) {
        super(cause);
    }

    public BucketAccessViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
