package com.petvax.care.exception;

public class PetNotFoundException extends RuntimeException {
    
    private final String errorCode;
    private final int statusCode;
    
    public PetNotFoundException(String message) {
        super(message);
        this.errorCode = "PET_NOT_FOUND";
        this.statusCode = 404;
    }
    
    public PetNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 404;
    }
    
    public PetNotFoundException(String message, String errorCode, int statusCode) {
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
