package com.vectorForJ.service.impl;

import com.vectorForJ.model.Vector;
import com.vectorForJ.service.VectorService;
import com.vectorForJ.storage.VectorStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class VectorServiceImpl implements VectorService {
    private final VectorStorage vectorStorage;
    private final ReentrantLock createLock = new ReentrantLock();

    @Autowired
    public VectorServiceImpl(VectorStorage vectorStorage) {
        this.vectorStorage = vectorStorage;
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