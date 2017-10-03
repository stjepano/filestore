package com.stjepano.filestore.client;

/**
 * Base FileStoreException ...
 */
public class FileStoreException extends RuntimeException {

    public FileStoreException() {
    }

    public FileStoreException(String message) {
        super(message);
    }

    public FileStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStoreException(Throwable cause) {
        super(cause);
    }

    public FileStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
