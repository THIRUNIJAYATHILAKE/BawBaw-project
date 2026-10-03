package com.petvax.care.exception;

public class AppointmentConflictException extends RuntimeException {
    
    private final String errorCode;
    private final int statusCode;
    
    public AppointmentConflictException(String message) {
        super(message);
        this.errorCode = "APPOINTMENT_CONFLICT";
        this.statusCode = 409;
    }
    
    public AppointmentConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 409;
    }
    
    public AppointmentConflictException(String message, String errorCode, int statusCode) {
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
