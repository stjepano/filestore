package com.stjepano.objectstore.service;

/**
 * Thrown when trying to create file that already exist
 */
public class FileAlreadyExistException extends ObjectStoreException {

    public FileAlreadyExistException(FileId fileId) {
        super(String.format("File '%s' already exists!", fileId));
    }

}
