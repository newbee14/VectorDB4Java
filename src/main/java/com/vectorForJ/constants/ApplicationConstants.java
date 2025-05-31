package com.vectorForJ.constants;

/**
 * Constants used throughout the application.
 * All hardcoded strings should be moved here for better maintainability.
 */
public final class ApplicationConstants {
    private ApplicationConstants() {
        // Private constructor to prevent instantiation
    }

    // API Related Constants
    public static final class Api {
        private Api() {}
        
        public static final String API_BASE_PATH = "/api";
        public static final String VECTORS_PATH = API_BASE_PATH + "/vectors";
        public static final String HEALTH_PATH = API_BASE_PATH + "/health";
        public static final String HEARTBEAT_PATH = "/heartbeat";
        
        // API Documentation
        public static final String API_TITLE = "VectorForJ API";
        public static final String API_VERSION = "1.0";
        public static final String API_DESCRIPTION = "In-memory Vector Database API for Java";
        
        // API Tags
        public static final String VECTOR_OPERATIONS_TAG = "Vector Operations";
        public static final String VECTOR_OPERATIONS_DESC = "API endpoints for vector database operations";
        public static final String HEALTH_CHECK_TAG = "Health Check";
        public static final String HEALTH_CHECK_DESC = "API endpoints for monitoring system health";
    }

    // Messages
    public static final class Messages {
        private Messages() {}
        
        // Health Check Messages
        public static final String SERVER_RUNNING = "Server is running";
        public static final String SERVER_STATUS_UP = "UP";
        
        // Error Messages
        public static final String VECTOR_NOT_FOUND = "Vector not found with id: %s";
        public static final String MALFORMED_JSON = "Malformed JSON request";
        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String MISSING_PARAMETER = "Missing Parameter";
        public static final String TYPE_MISMATCH = "Type Mismatch";
        public static final String INVALID_ARGUMENT = "Invalid Argument";
        public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
        public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
        
        // Parameter Messages
        public static final String PARAMETER_MISSING = "%s parameter is missing";
        public static final String PARAMETER_TYPE_MISMATCH = "Parameter '%s' of value '%s' could not be converted to type '%s'";
    }

    // Validation Messages
    public static final class Validation {
        private Validation() {}
        
        public static final String EMBEDDING_NOT_NULL = "Embedding cannot be null";
        public static final String DIMENSION_POSITIVE = "Dimension must be a positive number";
    }

    // Response Fields
    public static final class ResponseFields {
        private ResponseFields() {}
        
        public static final String TIMESTAMP = "timestamp";
        public static final String STATUS = "status";
        public static final String ERROR = "error";
        public static final String MESSAGE = "message";
        public static final String DETAILS = "details";
        public static final String TYPE = "type";
        public static final String MEMORY = "memory";
        public static final String UPTIME = "uptime";
        public static final String START_TIME = "startTime";
    }

    // Memory Stats Fields
    public static final class MemoryStats {
        private MemoryStats() {}
        
        public static final String TOTAL = "total";
        public static final String FREE = "free";
        public static final String USED = "used";
        public static final String MAX = "max";
    }

    // File Related
    public static final class File {
        private File() {}
        
        public static final String FILE_PARAM = "file";
        public static final String TEXT_PARAM = "text";
        public static final String TEXT_INPUT_TYPE = "text-input";
    }

    // Default Values
    public static final class Defaults {
        private Defaults() {}
        
        public static final String DEFAULT_K_VALUE = "10";
    }
} 