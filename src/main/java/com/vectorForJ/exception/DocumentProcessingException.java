package com.vectorForJ.exception;

public class DocumentProcessingException extends VectorDBException {
    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
} 