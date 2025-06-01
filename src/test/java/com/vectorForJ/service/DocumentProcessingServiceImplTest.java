package com.vectorForJ.service;

import com.vectorForJ.model.Vector;
import com.vectorForJ.service.impl.DocumentProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentProcessingServiceImplTest {

    @Autowired
    private DocumentProcessingServiceImpl documentProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set test-specific values directly
        ReflectionTestUtils.setField(documentProcessingService, "minWordsForEmbedding", 2);
        ReflectionTestUtils.setField(documentProcessingService, "unknownWordWeight", 0.1);
        ReflectionTestUtils.setField(documentProcessingService, "nounWeight", 1.2);
        ReflectionTestUtils.setField(documentProcessingService, "verbWeight", 1.1);
        ReflectionTestUtils.setField(documentProcessingService, "adjWeight", 1.05);
    }

    @Test
    void testProcessTextWithProperNouns() {
        // Given
        String text = "John Smith works at Google in New York";

        // When
        double[] embedding = documentProcessingService.generateEmbedding(text);

        // Then
        assertNotNull(embedding);
        assertTrue(embedding.length > 0);
    }

    @Test
    void testProcessTextWithKnownWords() {
        // Given
        String text = "The quick brown fox jumps over the lazy dog";

        // When
        double[] embedding = documentProcessingService.generateEmbedding(text);

        // Then
        assertNotNull(embedding);
        assertTrue(embedding.length > 0);
    }

    @Test
    void testProcessTextWithDuplicateDetection() {
        // Given
        String text = "This is a test sentence";

        // When
        double[] embedding = documentProcessingService.generateEmbedding(text);

        // Then
        assertNotNull(embedding);
        assertTrue(embedding.length > 0);
    }

    @Test
    void testProcessTextWithEmptyInput() {
        // Given
        String text = "";

        // When/Then
        assertThrows(RuntimeException.class, () -> documentProcessingService.generateEmbedding(text));
    }

    @Test
    void testProcessTextWithNullInput() {
        // Given
        String text = null;

        // When/Then
        assertThrows(RuntimeException.class, () -> documentProcessingService.generateEmbedding(text));
    }

    @Test
    void testProcessTextWithSpecialCharacters() {
        // Given
        String text = "Hello! This is a test... with some special characters: @#$%";

        // When
        double[] embedding = documentProcessingService.generateEmbedding(text);

        // Then
        assertNotNull(embedding);
        assertTrue(embedding.length > 0);
    }
} 