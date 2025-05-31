package com.vectorForJ.dao;

import com.vectorForJ.model.Vector;
import java.util.List;
import java.util.Optional;

public interface VectorDAO {
    Vector save(Vector vector);
    Optional<Vector> findById(String id);
    List<Vector> findAll();
    void delete(String id);
    List<Vector> findSimilar(double[] queryVector, int k);
} 