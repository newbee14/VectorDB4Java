package com.vectorForJ.indexing.common;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for vector operations used by both dense and sparse implementations.
 */
public class VectorUtils {
    private VectorUtils() {} // Prevent instantiation

    /**
     * Calculate cosine similarity between two dense vectors
     */
    public static double cosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Convert a dense vector to sparse representation
     * @return Map of dimension index to value for non-zero elements
     */
    public static Map<Integer, Double> toSparseVector(double[] denseVector) {
        Map<Integer, Double> sparseVector = new HashMap<>();
        for (int i = 0; i < denseVector.length; i++) {
            if (Math.abs(denseVector[i]) > 1e-10) { // Consider values close to zero as zero
                sparseVector.put(i, denseVector[i]);
            }
        }
        return sparseVector;
    }

    /**
     * Convert a sparse vector back to dense representation
     * @param sparseVector Map of dimension index to value
     * @param dimension Total vector dimension
     */
    public static double[] toDenseVector(Map<Integer, Double> sparseVector, int dimension) {
        double[] denseVector = new double[dimension];
        sparseVector.forEach((index, value) -> denseVector[index] = value);
        return denseVector;
    }

    /**
     * Calculate cosine similarity between two sparse vectors
     */
    public static double cosineSimilaritySparse(
            Map<Integer, Double> v1, Map<Integer, Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // Calculate dot product and norms
        for (Map.Entry<Integer, Double> entry : v1.entrySet()) {
            int index = entry.getKey();
            double value = entry.getValue();
            norm1 += value * value;
            if (v2.containsKey(index)) {
                dotProduct += value * v2.get(index);
            }
        }

        for (double value : v2.values()) {
            norm2 += value * value;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Normalize a vector to unit length
     */
    public static double[] normalize(double[] vector) {
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        if (norm == 0) {
            return vector;
        }

        double[] normalized = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }
        return normalized;
    }

    /**
     * Calculate memory usage of a vector in bytes
     */
    public static long calculateVectorMemoryUsage(double[] vector) {
        return vector.length * Double.BYTES;
    }

    /**
     * Calculate memory usage of a sparse vector in bytes
     */
    public static long calculateSparseVectorMemoryUsage(Map<Integer, Double> vector) {
        return vector.size() * (Integer.BYTES + Double.BYTES);
    }

    /**
     * Find top-k elements from a list of scores
     */
    public static <T> List<T> findTopK(List<T> items, List<Double> scores, int k) {
        if (items.size() != scores.size()) {
            throw new IllegalArgumentException("Items and scores must have same size");
        }

        // Create list of indices
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            indices.add(i);
        }

        // Sort indices based on scores
        indices.sort((i1, i2) -> Double.compare(scores.get(i2), scores.get(i1)));

        // Get top-k items
        List<T> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, items.size()); i++) {
            result.add(items.get(indices.get(i)));
        }

        return result;
    }
} 