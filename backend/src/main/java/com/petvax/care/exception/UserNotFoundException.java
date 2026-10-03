package com.petvax.care.exception;

public class UserNotFoundException extends RuntimeException {
    
    private final String errorCode;
    private final int statusCode;
    
    public UserNotFoundException(String message) {
        super(message);
        this.errorCode = "USER_NOT_FOUND";
        this.statusCode = 404;
    }
    
    public UserNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 404;
    }
    
    public UserNotFoundException(String message, String errorCode, int statusCode) {
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
