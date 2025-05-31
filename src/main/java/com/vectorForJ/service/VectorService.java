package com.vectorForJ.service;

import com.vectorForJ.model.Vector;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing vectors in the database.
 */
public interface VectorService {
    /**
     * Creates or updates a vector.
     */
    Vector createVector(Vector vector);

    /**
     * Retrieves a vector by ID.
     */
    Optional<Vector> getVector(String id);

    /**
     * Retrieves all vectors.
     */
    List<Vector> getAllVectors();

    /**
     * Deletes a vector by ID.
     */
    void deleteVector(String id);

    /**
     * Finds k most similar vectors to the query vector.
     */
    List<Vector> findSimilarVectors(double[] queryVector, int k);

    /**
     * Returns total number of vectors.
     */
    int getVectorCount();
} 