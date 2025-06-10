package com.vectorForJ.service;

import java.util.List;

/**
 * Service interface for generating context-aware embeddings using transformer models.
 * Provides methods for both single text and batch text processing with contextual understanding.
 */
public interface ContextAwareEmbeddingService {
    
    /**
     * Generate context-aware embedding for a single text input.
     * 
     * @param text The input text
     * @return Context-aware embedding vector
     */
    double[] generateContextAwareEmbedding(String text);
    
    /**
     * Generate context-aware embeddings for multiple texts in batch.
     * 
     * @param texts List of input texts
     * @return List of context-aware embedding vectors
     */
    List<double[]> generateBatchEmbeddings(List<String> texts);
    
    /**
     * Generate hybrid embedding combining static and contextual embeddings.
     * 
     * @param text The input text
     * @param staticEmbedding Pre-computed static embedding (e.g., from GloVe)
     * @return Hybrid embedding vector
     */
    double[] generateHybridEmbedding(String text, double[] staticEmbedding);
    
    /**
     * Check if the service is available and models are loaded.
     * 
     * @return true if service is ready, false otherwise
     */
    boolean isServiceAvailable();
} 