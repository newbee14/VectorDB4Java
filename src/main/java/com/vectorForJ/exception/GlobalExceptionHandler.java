package com.vectorForJ.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VectorNotFoundException.class)
    public ResponseEntity<Object> handleVectorNotFoundException(
            VectorNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<Object> handleDocumentProcessingException(
            DocumentProcessingException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VectorDBException.class)
    public ResponseEntity<Object> handleVectorDBException(
            VectorDBException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorResponse(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("type", ex.getClass().getSimpleName());
        return body;
    }
} 