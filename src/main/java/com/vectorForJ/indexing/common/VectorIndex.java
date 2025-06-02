package com.vectorForJ.indexing.common;

import com.vectorForJ.model.Vector;
import java.util.List;

/**
 * Base interface for vector indexing implementations.
 * Defines common operations for both dense and sparse vector indexing.
 */
public interface VectorIndex {
    /**
     * Add a vector to the index
     * @param vector The vector to add
     * @throws IllegalArgumentException if vector is null or invalid
     */
    void addVector(Vector vector);

    /**
     * Find k nearest neighbors for a query vector
     * @param queryVector The query vector
     * @param k Number of nearest neighbors to find
     * @param similarityThreshold Minimum similarity threshold (0.0 to 1.0)
     * @return List of k nearest neighbors, sorted by similarity
     */
    List<Vector> findNearestNeighbors(double[] queryVector, int k, double similarityThreshold);

    /**
     * Remove a vector from the index
     * @param vectorId The ID of the vector to remove
     * @return true if vector was found and removed, false otherwise
     */
    boolean removeVector(String vectorId);

    /**
     * Get the total number of vectors in the index
     * @return Number of vectors
     */
    int size();

    /**
     * Clear all vectors from the index
     */
    void clear();

    /**
     * Get the dimensionality of vectors in this index
     * @return Vector dimension
     */
    int getDimension();

    /**
     * Get the type of indexing strategy used
     * @return Index type (DENSE or SPARSE)
     */
    IndexType getIndexType();

    /**
     * Get performance statistics for the index
     * @return Index statistics
     */
    IndexStats getStats();
}

/**
 * Enum defining the type of indexing strategy
 */
enum IndexType {
    DENSE,   // Dense vector indexing (all dimensions used)
    SPARSE   // Sparse vector indexing (only non-zero dimensions)
}

/**
 * Class to hold index performance statistics
 */
class IndexStats {
    private final long totalQueries;
    private final long totalVectors;
    private final double averageQueryTime;
    private final double memoryUsage;
    private final double indexSize;

    public IndexStats(long totalQueries, long totalVectors, 
                     double averageQueryTime, double memoryUsage, double indexSize) {
        this.totalQueries = totalQueries;
        this.totalVectors = totalVectors;
        this.averageQueryTime = averageQueryTime;
        this.memoryUsage = memoryUsage;
        this.indexSize = indexSize;
    }

    // Getters
    public long getTotalQueries() { return totalQueries; }
    public long getTotalVectors() { return totalVectors; }
    public double getAverageQueryTime() { return averageQueryTime; }
    public double getMemoryUsage() { return memoryUsage; }
    public double getIndexSize() { return indexSize; }

    @Override
    public String toString() {
        return String.format(
            "IndexStats{totalQueries=%d, totalVectors=%d, avgQueryTime=%.2fms, " +
            "memoryUsage=%.2fMB, indexSize=%.2fMB}",
            totalQueries, totalVectors, averageQueryTime, memoryUsage, indexSize
        );
    }
} 