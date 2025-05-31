package com.vectorForJ.exception;

import com.vectorForJ.constants.ApplicationConstants.Messages;
import com.vectorForJ.constants.ApplicationConstants.ResponseFields;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.beans.TypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST endpoints.
 * Provides consistent error responses across the API.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles malformed JSON in requests.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.MESSAGE, Messages.MALFORMED_JSON);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * Handles validation failures.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.MESSAGE, Messages.VALIDATION_FAILED);
        body.put(ResponseFields.DETAILS, errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * Handles missing request parameters.
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String error = String.format(Messages.PARAMETER_MISSING, ex.getParameterName());
        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.MESSAGE, Messages.MISSING_PARAMETER);
        body.put(ResponseFields.DETAILS, error);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * Handles type mismatch in request parameters.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String error = String.format(
            Messages.PARAMETER_TYPE_MISMATCH,
            ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()
        );
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            Messages.TYPE_MISMATCH,
            List.of(error)
        );
    }

    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(
            IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.MESSAGE, Messages.INVALID_ARGUMENT);
        body.put(ResponseFields.DETAILS, ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * Handles all uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllUncaughtException(
            Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.MESSAGE, Messages.INTERNAL_SERVER_ERROR);
        body.put(ResponseFields.DETAILS, Messages.UNEXPECTED_ERROR);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, body);
    }

    /**
     * Builds error response with single error message.
     */
    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status, Map<String, Object> body) {
        body.put(ResponseFields.TIMESTAMP, LocalDateTime.now());
        body.put(ResponseFields.STATUS, status.value());
        body.put(ResponseFields.ERROR, status.getReasonPhrase());
        return new ResponseEntity<>(body, status);
    }

    /**
     * Builds error response with multiple error messages.
     */
    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status, String message, List<String> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put(ResponseFields.TIMESTAMP, LocalDateTime.now());
        body.put(ResponseFields.STATUS, status.value());
        body.put(ResponseFields.ERROR, status.getReasonPhrase());
        body.put(ResponseFields.MESSAGE, message);
        body.put(ResponseFields.DETAILS, errors);
        return new ResponseEntity<>(body, status);
    }
} 