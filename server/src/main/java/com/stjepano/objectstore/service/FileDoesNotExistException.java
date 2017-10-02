package com.stjepano.objectstore.service;

/**
 * Thrown when trying to retrieve file that does not exist
 */
public class FileDoesNotExistException extends ObjectStoreException {

    public FileDoesNotExistException(FileId fileId) {
        super(String.format("File '%s' does not exist!", fileId));
    }
}
