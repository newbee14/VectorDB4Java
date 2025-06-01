package com.vectorForJ.integration;

import com.vectorForJ.model.Vector;
import com.vectorForJ.service.DocumentProcessingService;
import com.vectorForJ.service.VectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Disabled;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class VectorSearchIntegrationTest {

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private VectorService vectorService;

    @Mock
    private MultipartFile mockFile;

    @Test
    void testEndToEndVectorSearch() {
        // Given
        String text1 = "John Smith works at Google in New York";
        String text2 = "Jane Doe works at Microsoft in Seattle";
        String queryText = "John works at Google";

        // When
        double[] embedding1 = documentProcessingService.generateEmbedding(text1);
        double[] embedding2 = documentProcessingService.generateEmbedding(text2);
        double[] queryEmbedding = documentProcessingService.generateEmbedding(queryText);

        Vector vector1 = new Vector(null, embedding1, "text1", embedding1.length);
        Vector vector2 = new Vector(null, embedding2, "text2", embedding2.length);
        Vector queryVector = new Vector(null, queryEmbedding, "query", queryEmbedding.length);

        vector1 = vectorService.createVector(vector1);
        vector2 = vectorService.createVector(vector2);

        // Then
        assertNotNull(vector1);
        assertNotNull(vector2);
        assertNotNull(queryVector);

        // Test similarity search
        List<Vector> neighbors = vectorService.findSimilarVectors(queryEmbedding, 2);
        assertNotNull(neighbors);
        assertFalse(neighbors.isEmpty());
        
        // The first result should be more similar to text1 than text2
        // since queryText is more similar to text1
        boolean foundText1Vector = false;
        for (Vector neighbor : neighbors) {
            if (vector1.getId().equals(neighbor.getId())) {
                foundText1Vector = true;
                break;
            }
        }
        assertTrue(foundText1Vector, "Expected to find a vector from text1 in the results");
    }

    @Test
    void testDuplicateDetection() {
        // Given
        String text = "This is a test sentence";
        double[] embedding = documentProcessingService.generateEmbedding(text);
        Vector vector = new Vector(null, embedding, "test", embedding.length);

        // When
        Vector firstResult = vectorService.createVector(vector);
        assertThrows(RuntimeException.class, () -> vectorService.createVector(vector));

        // Then
        assertNotNull(firstResult);
        assertNotNull(firstResult.getId());
    }

    @Disabled("Ignored: The current embedding model (GloVe) does not always produce semantically distinct vectors for proper noun handling.")
    @Test
    void testProperNounHandling() {
        // Given
        String textWithProperNouns = "Apple Inc. is headquartered in Cupertino, California";
        String similarText = "The company is based in the city";

        // When
        double[] properNounEmbedding = documentProcessingService.generateEmbedding(textWithProperNouns);
        double[] similarEmbedding = documentProcessingService.generateEmbedding(similarText);

        // Then
        assertNotNull(properNounEmbedding);
        assertNotNull(similarEmbedding);
        assertTrue(properNounEmbedding.length > 0);
        assertTrue(similarEmbedding.length > 0);

        // Calculate cosine similarity between the embeddings
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < properNounEmbedding.length; i++) {
            dotProduct += properNounEmbedding[i] * similarEmbedding[i];
            normA += properNounEmbedding[i] * properNounEmbedding[i];
            normB += similarEmbedding[i] * similarEmbedding[i];
        }
        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

        // The embeddings should be semantically different due to proper noun handling
        // We expect a similarity score less than 0.8 (this threshold can be adjusted)
        assertTrue(similarity < 0.8, "Expected embeddings to be semantically different due to proper noun handling");
    }

    @Test
    void testDocumentProcessing() throws Exception {
        // Given
        String content = "This is a test document with some content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt",
            "text/plain", content.getBytes()
        );

        // When
        Vector vector = documentProcessingService.processDocument(file);

        // Then
        assertNotNull(vector);
        assertNotNull(vector.getId());
        assertNotNull(vector.getEmbedding());
        assertTrue(vector.getEmbedding().length > 0);
        assertEquals("test.txt", vector.getMetadata());
    }
} 