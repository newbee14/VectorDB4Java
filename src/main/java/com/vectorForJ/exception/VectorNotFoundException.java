package com.vectorForJ.exception;

import com.vectorForJ.constants.ApplicationConstants.Messages;

public class VectorNotFoundException extends RuntimeException {
    public VectorNotFoundException(String id) {
        super(String.format(Messages.VECTOR_NOT_FOUND, id));
    }
} 