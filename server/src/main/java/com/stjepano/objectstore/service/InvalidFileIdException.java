package com.stjepano.objectstore.service;

/**
 * Thrown if file name is invalid
 */
public class InvalidFileIdException extends IllegalArgumentException {

    public InvalidFileIdException(String fileName) {
        super(String.format("File name '%s' is not valid!", fileName == null ? "null" : fileName));
    }

}
