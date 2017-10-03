package com.stjepano.filestore.common;

/**
 * Generic error response which is returned in case of exception.
 */
public class ErrorResponse {
    private boolean error;
    private String message;

    public ErrorResponse() {

    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
