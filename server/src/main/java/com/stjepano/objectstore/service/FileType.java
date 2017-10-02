package com.stjepano.objectstore.service;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A file type
 */
public enum FileType {
    REGULAR_FILE("regular"),
    DIRECTORY("directory");

    private final String code;

    FileType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static FileType from(String str) {
        if (str.equals("regular")) return REGULAR_FILE;
        if (str.equals("directory")) return DIRECTORY;
        throw new IllegalArgumentException(str);
    }
}
