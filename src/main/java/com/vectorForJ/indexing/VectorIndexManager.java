package com.vectorForJ.indexing;

import com.vectorForJ.model.Vector;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import java.io.*;
import java.util.*;

@Component
public class VectorIndexManager {
    private static final Logger logger = LoggerFactory.getLogger(VectorIndexManager.class);
    private static final String INDEX_FILE = "vector_index.dat";
    private static final String VECTOR_FIELD = "embedding";
    private static final String ID_FIELD = "id";
    private static final int VECTOR_DIMENSION = 1536; // Adjust as needed

    private final ByteBuffersDirectory directory = new ByteBuffersDirectory();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriter indexWriter;

    public VectorIndexManager() {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            this.indexWriter = new IndexWriter(directory, config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Lucene index", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            File indexFile = new File(INDEX_FILE);
            if (indexFile.exists()) {
                // Load vectors
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INDEX_FILE))) {
                    Map<Integer, double[]> loadedVectors = (Map<Integer, double[]>) ois.readObject();
                    for (Map.Entry<Integer, double[]> entry : loadedVectors.entrySet()) {
                        double[] embedding = entry.getValue();
                        addVector(new com.vectorForJ.model.Vector(entry.getKey().toString(), embedding, "", embedding.length));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to initialize vector index", e);
            throw new RuntimeException("Failed to initialize vector index", e);
        }
    }

    public void addVector(Vector vector) {
        try {
            removeVector(vector.getId()); // Remove if exists
            Document doc = new Document();
            doc.add(new StringField(ID_FIELD, vector.getId(), Field.Store.YES));
            double[] embedding = vector.getEmbedding();
            float[] floatEmbedding = new float[embedding.length];
            for (int i = 0; i < embedding.length; i++) floatEmbedding[i] = (float) embedding[i];
            doc.add(new KnnVectorField(VECTOR_FIELD, floatEmbedding));
            indexWriter.addDocument(doc);
            indexWriter.commit();
        } catch (IOException e) {
            logger.error("Failed to add vector to Lucene index", e);
            throw new RuntimeException("Failed to add vector to Lucene index", e);
        }
    }

    public void removeVector(String id) {
        try {
            indexWriter.deleteDocuments(new Term(ID_FIELD, id));
            indexWriter.commit();
        } catch (IOException e) {
            logger.error("Failed to remove vector from Lucene index", e);
            throw new RuntimeException("Failed to remove vector from Lucene index", e);
        }
    }

    public List<String> findNearestNeighbors(double[] queryVector, int k) {
        try (DirectoryReader reader = DirectoryReader.open(indexWriter)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            float[] floatQuery = new float[queryVector.length];
            for (int i = 0; i < queryVector.length; i++) floatQuery[i] = (float) queryVector[i];
            Query knnQuery = new KnnVectorQuery(VECTOR_FIELD, floatQuery, k);
            TopDocs topDocs = searcher.search(knnQuery, k);
            List<String> result = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                result.add(doc.get(ID_FIELD));
            }
            return result;
        } catch (IOException e) {
            logger.error("Failed to search Lucene index", e);
            throw new RuntimeException("Failed to search Lucene index", e);
        }
    }

    public boolean containsVector(String id) {
        try (DirectoryReader reader = DirectoryReader.open(indexWriter)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new TermQuery(new Term(ID_FIELD, id));
            TopDocs topDocs = searcher.search(query, 1);
            return topDocs.totalHits.value > 0;
        } catch (IOException e) {
            logger.error("Failed to check vector existence in Lucene index", e);
            throw new RuntimeException("Failed to check vector existence in Lucene index", e);
        }
    }

    public int getIndexSize() {
        try (DirectoryReader reader = DirectoryReader.open(indexWriter)) {
            return reader.numDocs();
        } catch (IOException e) {
            logger.error("Failed to get Lucene index size", e);
            throw new RuntimeException("Failed to get Lucene index size", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            indexWriter.close();
        } catch (IOException e) {
            logger.error("Failed to close Lucene index during cleanup", e);
        }
    }
} 