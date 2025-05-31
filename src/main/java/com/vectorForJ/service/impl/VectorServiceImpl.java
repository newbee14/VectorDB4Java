package com.vectorForJ.service.impl;

import com.vectorForJ.model.Vector;
import com.vectorForJ.service.VectorService;
import com.vectorForJ.storage.VectorStorage;
import com.vectorForJ.exception.VectorDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class VectorServiceImpl implements VectorService {
    private static final Logger logger = LoggerFactory.getLogger(VectorServiceImpl.class);
    private final VectorStorage vectorStorage;
    private final ReentrantLock createLock = new ReentrantLock();

    @Value("${vector.similarity.threshold:0.95}")
    private double similarityThreshold;

    @Autowired
    public VectorServiceImpl(VectorStorage vectorStorage) {
        this.vectorStorage = vectorStorage;
    }

    private boolean isSimilarToExisting(double[] embedding) {
        // Find the most similar vector
        List<Vector> similarVectors = vectorStorage.findNearest(embedding, 1);
        if (similarVectors.isEmpty()) {
            return false;
        }

        Vector mostSimilar = similarVectors.get(0);
        double similarity = cosineSimilarity(embedding, mostSimilar.getEmbedding());
        logger.debug("Found similar vector with similarity score: {}", similarity);
        return similarity >= similarityThreshold;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public Vector createVector(Vector vector) {
        Assert.notNull(vector, "Vector cannot be null");
        Assert.notNull(vector.getEmbedding(), "Vector embedding cannot be null");
        Assert.isTrue(vector.getDimension() > 0, "Vector dimension must be positive");
        Assert.isTrue(vector.getEmbedding().length == vector.getDimension(), 
            "Vector dimension must match embedding length");

        createLock.lock();
        try {
            // Check for similar vectors
            if (isSimilarToExisting(vector.getEmbedding())) {
                logger.warn("Similar vector already exists");
                throw new VectorDBException("A similar vector already exists in the database");
            }

            if (vector.getId() == null) {
                vector.setId(UUID.randomUUID().toString());
            }
            vectorStorage.store(vector);
            return vector;
        } finally {
            createLock.unlock();
        }
    }

    @Override
    public Optional<Vector> getVector(String id) {
        Assert.hasText(id, "Vector ID cannot be null or empty");
        return vectorStorage.retrieve(id);
    }

    @Override
    public List<Vector> getAllVectors() {
        return vectorStorage.retrieveAll();
    }

    @Override
    public void deleteVector(String id) {
        Assert.hasText(id, "Vector ID cannot be null or empty");
        vectorStorage.remove(id);
    }

    @Override
    public List<Vector> findSimilarVectors(double[] queryVector, int k) {
        Assert.notNull(queryVector, "Query vector cannot be null");
        Assert.isTrue(k > 0, "Number of similar vectors must be positive");
        return vectorStorage.findNearest(queryVector, k);
    }

    @Override
    public int getVectorCount() {
        return vectorStorage.size();
    }
} 