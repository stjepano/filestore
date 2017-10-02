package com.stjepano.objectstore.service;

/**
 * Generic objectstore exception
 */
public class ObjectStoreException extends Exception {

    public ObjectStoreException() {
    }

    public ObjectStoreException(String message) {
        super(message);
    }

    public ObjectStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectStoreException(Throwable cause) {
        super(cause);
    }

    public ObjectStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
