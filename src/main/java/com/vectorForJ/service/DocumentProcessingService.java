package com.vectorForJ.service;

import org.springframework.web.multipart.MultipartFile;
import com.vectorForJ.model.Vector;

/**
 * Service for processing documents and generating vector embeddings.
 */
public interface DocumentProcessingService {
    /**
     * Extracts text content from a document file.
     */
    String extractText(MultipartFile file);

    /**
     * Generates vector embedding from text content.
     */
    double[] generateEmbedding(String text);

    /**
     * Processes a document file and creates a vector.
     */
    Vector processDocument(MultipartFile file);
} 