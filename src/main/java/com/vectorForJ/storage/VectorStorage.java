package com.vectorForJ.storage;

import com.vectorForJ.model.Vector;
import java.util.List;
import java.util.Optional;

public interface VectorStorage {
    void store(Vector vector);
    Optional<Vector> retrieve(String id);
    List<Vector> retrieveAll();
    void remove(String id);
    List<Vector> findNearest(double[] queryVector, int k);
    int size();
} 