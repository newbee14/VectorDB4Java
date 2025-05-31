package com.vectorForJ.service.impl;

import com.vectorForJ.exception.DocumentProcessingException;
import com.vectorForJ.model.Vector;
import com.vectorForJ.service.DocumentProcessingService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingServiceImpl.class);
    private final Tika tika;
    private final Word2Vec word2Vec;
    private static final String MODEL_PATH = "models/glove.6B.100d.txt"; // Using the 100-dimensional GloVe model

    public DocumentProcessingServiceImpl() {
        this.tika = new Tika();
        try {
            // Load pre-trained model
            File modelFile = new File(MODEL_PATH);
            if (!modelFile.exists()) {
                logger.warn("Pre-trained model not found at {}. Using a small random model instead.", MODEL_PATH);
                // Fallback to a small random model if pre-trained model is not available
                this.word2Vec = new Word2Vec.Builder()
                     .minWordFrequency(1)
                     .iterations(1)
                     .layerSize(100)
                     .seed(42)
                     .windowSize(5)
                     .build();
            } else {
                logger.info("Loading pre-trained GloVe model from {} (size: {} bytes)", MODEL_PATH, modelFile.length());
                this.word2Vec = WordVectorSerializer.readWord2VecModel(modelFile);
                logger.info("Successfully loaded pre-trained model. Vocabulary size: {}", word2Vec.vocab().numWords());
            }
        } catch (Exception e) {
            logger.error("Error loading pre-trained model from {}: {}", MODEL_PATH, e.getMessage(), e);
            throw new DocumentProcessingException("Failed to initialize word embedding model", e);
        }
    }

    @Override
    public String extractText(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            return tika.parseToString(bis);
        } catch (IOException | TikaException e) {
            throw new DocumentProcessingException("Failed to extract text from document", e);
        }
    }

    @Override
    public double[] generateEmbedding(String text) {
        try {
            // Generate document embedding by averaging word vectors
            double[] embedding = new double[word2Vec.getLayerSize()];
            int wordCount = 0;
            
            for (String word : text.toLowerCase().split("\\s+")) {
                if (word2Vec.hasWord(word)) {
                    double[] wordVector = word2Vec.getWordVector(word);
                    for (int i = 0; i < wordVector.length; i++) {
                        embedding[i] += wordVector[i];
                    }
                    wordCount++;
                }
            }
            
            // Normalize the embedding
            if (wordCount > 0) {
                for (int i = 0; i < embedding.length; i++) {
                    embedding[i] /= wordCount;
                }
            } else {
                logger.warn("No known words found in text for embedding generation");
            }
            
            return embedding;
        } catch (Exception e) {
            logger.error("Error generating embedding: {}", e.getMessage());
            throw new DocumentProcessingException("Failed to generate embedding", e);
        }
    }

    @Override
    public Vector processDocument(MultipartFile file) {
        String text = extractText(file);
        double[] embedding = generateEmbedding(text);
        
        return new Vector(
            UUID.randomUUID().toString(),
            embedding,
            file.getOriginalFilename(),
            embedding.length
        );
    }
} 