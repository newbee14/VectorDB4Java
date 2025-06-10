package com.vectorForJ.service;

import com.vectorForJ.service.impl.ContextAwareEmbeddingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ContextAwareEmbeddingService
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "embeddings.context.enabled=false"  // Disable for testing since we don't have PyTorch in CI
})
class ContextAwareEmbeddingServiceTest {

    private ContextAwareEmbeddingService contextAwareEmbeddingService;

    @BeforeEach
    void setUp() {
        contextAwareEmbeddingService = new ContextAwareEmbeddingServiceImpl();
    }

    @Test
    void testServiceAvailability() {
        // Since PyTorch is not available in test environment, service should not be available
        assertFalse(contextAwareEmbeddingService.isServiceAvailable());
    }

    @Test
    void testGenerateHybridEmbeddingWithoutService() {
        // When service is not available, hybrid embedding should return static embedding
        double[] staticEmbedding = {0.1, 0.2, 0.3, 0.4, 0.5};
        String text = "This is a test sentence";
        
        double[] result = contextAwareEmbeddingService.generateHybridEmbedding(text, staticEmbedding);
        
        // Should return the static embedding when service is not available
        assertArrayEquals(staticEmbedding, result);
    }

    @Test
    void testGenerateContextAwareEmbeddingThrowsExceptionWhenNotAvailable() {
        // Should throw exception when service is not available
        assertThrows(Exception.class, () -> {
            contextAwareEmbeddingService.generateContextAwareEmbedding("test text");
        });
    }

    @Test
    void testGenerateBatchEmbeddingsThrowsExceptionWhenNotAvailable() {
        // Should throw exception when service is not available
        assertThrows(Exception.class, () -> {
            contextAwareEmbeddingService.generateBatchEmbeddings(java.util.Arrays.asList("test text"));
        });
    }
} 