package com.stjepano.filestore.client;

/**
 * Thrown when server returns error response (code >= 400)
 */
public class FileStoreServerException extends FileStoreException {
    private final int code;
    private final String serverMessage;

    public FileStoreServerException(int code, String message) {
        super(String.format("Server failed with response code %d, message: '%s'", code, message == null ? "null" : message));
        this.code = code;
        this.serverMessage = message;
    }

    public int getCode() {
        return code;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
