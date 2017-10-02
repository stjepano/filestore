package com.stjepano.filestore.service;

/**
 * Thrown if file name is invalid
 */
public class InvalidFileIdException extends IllegalArgumentException {

    public InvalidFileIdException(String fileName) {
        super(String.format("File name '%s' is not valid!", fileName == null ? "null" : fileName));
    }

    public InvalidFileIdException(InvalidBucketIdException e) {
        super("File id invalid because of: " + e.getMessage(), e);
    }

}
