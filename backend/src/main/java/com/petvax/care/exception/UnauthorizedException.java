package com.petvax.care.exception;

public class UnauthorizedException extends RuntimeException {
    
    private final String errorCode;
    private final int statusCode;
    
    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = "UNAUTHORIZED";
        this.statusCode = 403;
    }
    
    public UnauthorizedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 403;
    }
    
    public UnauthorizedException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
