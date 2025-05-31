package com.vectorForJ.exception;

public class VectorDBException extends RuntimeException {
    public VectorDBException(String message) {
        super(message);
    }

    public VectorDBException(String message, Throwable cause) {
        super(message, cause);
    }
} 