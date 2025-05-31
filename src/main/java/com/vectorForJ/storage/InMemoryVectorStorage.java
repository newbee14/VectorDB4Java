package com.vectorForJ.storage;

import com.vectorForJ.indexing.VectorIndexManager;
import com.vectorForJ.model.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class InMemoryVectorStorage implements VectorStorage {
    private final Map<String, Vector> vectors = new ConcurrentHashMap<>();
    private final AtomicInteger vectorCount = new AtomicInteger(0);
    private final VectorIndexManager indexManager;

    @Autowired
    public InMemoryVectorStorage(VectorIndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Override
    public void store(Vector vector) {
        vectors.put(vector.getId(), vector);
        indexManager.addVector(vector);
        vectorCount.incrementAndGet();
    }

    @Override
    public Optional<Vector> retrieve(String id) {
        return Optional.ofNullable(vectors.get(id));
    }

    @Override
    public List<Vector> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(vectors.values()));
    }

    @Override
    public void remove(String id) {
        if (vectors.remove(id) != null) {
            indexManager.removeVector(id);
            vectorCount.decrementAndGet();
        }
    }

    @Override
    public List<Vector> findNearest(double[] queryVector, int k) {
        List<String> nearestIds = indexManager.findNearestNeighbors(queryVector, k);
        return nearestIds.stream()
                .map(vectors::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public int size() {
        return vectorCount.get();
    }
} 