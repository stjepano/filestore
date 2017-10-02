package com.stjepano.objectstore;

/**
 * Generic error response which is returned in case of exception.
 */
public class ErrorResponse {
    private final boolean error = true;
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
