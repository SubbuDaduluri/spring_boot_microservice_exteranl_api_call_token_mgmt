package com.subbu.dsrmtech.externalapicall.exception;

public class OAuth2TokenException extends RuntimeException {
    private int statusCode;

    public OAuth2TokenException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public OAuth2TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuth2TokenException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
