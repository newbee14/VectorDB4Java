package com.vectorForJ.service;

import com.vectorForJ.model.Vector;
import com.vectorForJ.storage.VectorStorage;
import com.vectorForJ.service.impl.VectorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VectorServiceImplTest {
    private static final double SIMILARITY_THRESHOLD = 0.95;

    private VectorServiceImpl vectorService;

    @Mock
    private VectorStorage vectorStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vectorService = new VectorServiceImpl(vectorStorage);
        ReflectionTestUtils.setField(vectorService, "similarityThreshold", SIMILARITY_THRESHOLD);
    }

    @Test
    void testCreateVector() {
        // Given
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        Vector vector = new Vector(null, embedding, "test", embedding.length);
        when(vectorStorage.findNearest(embedding, 1)).thenReturn(List.of());
        doNothing().when(vectorStorage).store(any(Vector.class));

        // When
        Vector result = vectorService.createVector(vector);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(vectorStorage).store(any(Vector.class));
    }

    @Test
    void testFindSimilarVectors() {
        // Given
        double[] queryEmbedding = new double[]{0.1, 0.2, 0.3};
        Vector similarVector = new Vector("1", new double[]{0.11, 0.21, 0.31}, "test", 3);
        when(vectorStorage.findNearest(queryEmbedding, 5)).thenReturn(List.of(similarVector));

        // When
        List<Vector> results = vectorService.findSimilarVectors(queryEmbedding, 5);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(similarVector.getId(), results.get(0).getId());
    }

    @Test
    void testDeleteVector() {
        // Given
        String vectorId = "test-id";
        doNothing().when(vectorStorage).remove(vectorId);

        // When
        vectorService.deleteVector(vectorId);

        // Then
        verify(vectorStorage).remove(vectorId);
    }

    @Test
    void testDuplicateDetection() {
        // Given
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        Vector vector = new Vector(null, embedding, "test", embedding.length);
        Vector existingVector = new Vector("1", embedding, "test", embedding.length);
        when(vectorStorage.findNearest(embedding, 1)).thenReturn(List.of(existingVector));

        // When/Then
        assertThrows(RuntimeException.class, () -> vectorService.createVector(vector));
    }
} 