package com.vectorForJ.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.vectorForJ.constants.ApplicationConstants.Api;
import com.vectorForJ.constants.ApplicationConstants.Messages;
import com.vectorForJ.constants.ApplicationConstants.ResponseFields;
import com.vectorForJ.constants.ApplicationConstants.MemoryStats;

/**
 * Controller for health check and heartbeat endpoints.
 */
@RestController
@RequestMapping(Api.HEALTH_PATH)
@Tag(name = Api.HEALTH_CHECK_TAG, description = Api.HEALTH_CHECK_DESC)
public class HealthController {
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * Simple health check endpoint.
     */
    @Operation(
        summary = "Basic health check",
        description = "Returns a simple status indicating if the server is running"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Server is running normally"
    )
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check requested");
        return ResponseEntity.ok(Messages.SERVER_RUNNING);
    }

    /**
     * Detailed heartbeat endpoint with server stats.
     */
    @Operation(
        summary = "Detailed health check",
        description = "Returns detailed system statistics including memory usage and uptime"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Detailed system health information"
    )
    @GetMapping(Api.HEARTBEAT_PATH)
    public ResponseEntity<Map<String, Object>> heartbeat() {
        logger.info("Heartbeat requested");
        
        Map<String, Object> status = new HashMap<>();
        status.put(ResponseFields.STATUS, Messages.SERVER_STATUS_UP);
        status.put(ResponseFields.TIMESTAMP, LocalDateTime.now());
        status.put(ResponseFields.START_TIME, startTime);
        status.put(ResponseFields.UPTIME, String.format("%s seconds", 
            Duration.between(startTime, LocalDateTime.now()).getSeconds()));
        status.put(ResponseFields.MEMORY, getMemoryStats());
        
        return ResponseEntity.ok(status);
    }

    private Map<String, String> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> memory = new HashMap<>();
        memory.put(MemoryStats.TOTAL, formatSize(runtime.totalMemory()));
        memory.put(MemoryStats.FREE, formatSize(runtime.freeMemory()));
        memory.put(MemoryStats.USED, formatSize(runtime.totalMemory() - runtime.freeMemory()));
        memory.put(MemoryStats.MAX, formatSize(runtime.maxMemory()));
        return memory;
    }

    private String formatSize(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
} 