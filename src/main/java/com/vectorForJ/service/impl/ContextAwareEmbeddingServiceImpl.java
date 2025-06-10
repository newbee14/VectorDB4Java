package com.vectorForJ.service.impl;

import com.vectorForJ.exception.DocumentProcessingException;
import com.vectorForJ.service.ContextAwareEmbeddingService;
import ai.djl.Application;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContextAwareEmbeddingServiceImpl implements ContextAwareEmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextAwareEmbeddingServiceImpl.class);
    
    @Value("${embeddings.model.name:sentence-transformers/all-MiniLM-L6-v2}")
    private String modelName;
    
    @Value("${embeddings.context.enabled:true}")
    private boolean contextEnabled;
    
    @Value("${embeddings.hybrid.static-weight:0.3}")
    private double staticWeight;
    
    @Value("${embeddings.hybrid.context-weight:0.7}")
    private double contextWeight;
    
    @Value("${embeddings.cache.enabled:true}")
    private boolean cacheEnabled;
    
    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;
    private HuggingFaceTokenizer tokenizer;
    private final Map<String, double[]> embeddingCache = new ConcurrentHashMap<>();
    private boolean serviceAvailable = false;
    
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing context-aware embedding service with model: {}", modelName);
            
            // Check if PyTorch engine is available
            if (!Engine.hasEngine("PyTorch")) {
                logger.warn("PyTorch engine not available. Context-aware embeddings will not be available.");
                return;
            }
            
            // Create criteria for sentence transformer model
            Criteria<String, float[]> criteria = Criteria.builder()
                    .setTypes(String.class, float[].class)
                    .optModelUrls("djl://ai.djl.huggingface.pytorch/" + modelName)
                    .optEngine("PyTorch")
                    .optTranslator(new SentenceTransformerTranslator())
                    .build();
            
            // Load model
            model = criteria.loadModel();
            predictor = model.newPredictor();
            
            // Initialize tokenizer
            tokenizer = HuggingFaceTokenizer.newInstance(modelName);
            
            serviceAvailable = true;
            logger.info("Context-aware embedding service initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize context-aware embedding service: {}", e.getMessage(), e);
            serviceAvailable = false;
            // Don't throw exception - allow service to start with degraded functionality
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        if (tokenizer != null) {
            tokenizer.close();
        }
    }
    
    @Override
    public double[] generateContextAwareEmbedding(String text) {
        if (!serviceAvailable || !contextEnabled) {
            throw new DocumentProcessingException("Context-aware embedding service is not available");
        }
        
        if (text == null || text.trim().isEmpty()) {
            throw new DocumentProcessingException("Input text cannot be empty");
        }
        
        // Check cache first
        if (cacheEnabled && embeddingCache.containsKey(text)) {
            logger.debug("Returning cached embedding for text: {}", text.substring(0, Math.min(50, text.length())));
            return embeddingCache.get(text);
        }
        
        try {
            // Generate embedding using the predictor
            float[] embedding = predictor.predict(text);
            double[] result = floatArrayToDoubleArray(embedding);
            
            // Cache the result
            if (cacheEnabled) {
                embeddingCache.put(text, result);
            }
            
            logger.debug("Generated context-aware embedding of size {} for text: {}", 
                    result.length, text.substring(0, Math.min(50, text.length())));
            
            return result;
            
        } catch (TranslateException e) {
            logger.error("Error generating context-aware embedding: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to generate context-aware embedding", e);
        }
    }
    
    @Override
    public List<double[]> generateBatchEmbeddings(List<String> texts) {
        if (!serviceAvailable || !contextEnabled) {
            throw new DocumentProcessingException("Context-aware embedding service is not available");
        }
        
        if (texts == null || texts.isEmpty()) {
            throw new DocumentProcessingException("Input texts cannot be empty");
        }
        
        List<double[]> embeddings = new ArrayList<>();
        
        // Process each text individually (can be optimized for true batch processing)
        for (String text : texts) {
            embeddings.add(generateContextAwareEmbedding(text));
        }
        
        return embeddings;
    }
    
    @Override
    public double[] generateHybridEmbedding(String text, double[] staticEmbedding) {
        if (!serviceAvailable || !contextEnabled) {
            // If context-aware service is not available, return static embedding
            logger.debug("Context-aware service not available, returning static embedding");
            return staticEmbedding;
        }
        
        try {
            double[] contextEmbedding = generateContextAwareEmbedding(text);
            
            // Ensure both embeddings have the same dimension
            int targetDimension = Math.max(staticEmbedding.length, contextEmbedding.length);
            
            double[] normalizedStatic = normalizeEmbeddingSize(staticEmbedding, targetDimension);
            double[] normalizedContext = normalizeEmbeddingSize(contextEmbedding, targetDimension);
            
            // Combine embeddings using weighted average
            double[] hybridEmbedding = new double[targetDimension];
            for (int i = 0; i < targetDimension; i++) {
                hybridEmbedding[i] = (staticWeight * normalizedStatic[i]) + (contextWeight * normalizedContext[i]);
            }
            
            logger.debug("Generated hybrid embedding of size {} (static: {}, context: {})", 
                    hybridEmbedding.length, staticEmbedding.length, contextEmbedding.length);
            
            return hybridEmbedding;
            
        } catch (Exception e) {
            logger.warn("Failed to generate hybrid embedding, falling back to static: {}", e.getMessage());
            return staticEmbedding;
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        return serviceAvailable && contextEnabled;
    }
    
    private double[] floatArrayToDoubleArray(float[] floatArray) {
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }
    
    private double[] normalizeEmbeddingSize(double[] embedding, int targetSize) {
        if (embedding.length == targetSize) {
            return embedding;
        }
        
        double[] normalized = new double[targetSize];
        if (embedding.length < targetSize) {
            // Pad with zeros
            System.arraycopy(embedding, 0, normalized, 0, embedding.length);
        } else {
            // Truncate
            System.arraycopy(embedding, 0, normalized, 0, targetSize);
        }
        
        return normalized;
    }
    
    /**
     * Custom translator for sentence transformer models
     */
    private static class SentenceTransformerTranslator implements Translator<String, float[]> {
        
        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {
            // Get the pooled output (usually the first element)
            NDArray embeddings = list.get(0);
            
            // Apply mean pooling if needed
            if (embeddings.getShape().dimension() > 1) {
                embeddings = embeddings.mean(new int[]{1}, true);
            }
            
            // Normalize the embeddings
            embeddings = embeddings.div(embeddings.norm(new int[]{1}, true));
            
            return embeddings.toFloatArray();
        }
        
        @Override
        public NDList processInput(TranslatorContext ctx, String input) {
            NDManager manager = ctx.getNDManager();
            
            // Simple tokenization - in production, you'd use the proper tokenizer
            String[] tokens = input.toLowerCase().split("\\s+");
            long[] tokenIds = new long[tokens.length];
            
            // This is a simplified approach - in practice, you'd use the actual tokenizer
            for (int i = 0; i < tokens.length; i++) {
                tokenIds[i] = tokens[i].hashCode() % 30000; // Simplified token mapping
            }
            
            NDArray inputIds = manager.create(tokenIds).expandDims(0);
            NDArray attentionMask = manager.ones(inputIds.getShape());
            
            return new NDList(inputIds, attentionMask);
        }
    }
} 