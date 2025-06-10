package com.vectorForJ.service.impl;

import com.vectorForJ.exception.DocumentProcessingException;
import com.vectorForJ.model.Vector;
import com.vectorForJ.service.DocumentProcessingService;
import com.vectorForJ.service.ContextAwareEmbeddingService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingServiceImpl.class);
    private final Tika tika;
    private final Word2Vec word2Vec;
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final StanfordCoreNLP pipeline;
    private final TokenizerFactory tokenizerFactory;

    private static final String MODEL_PATH = "src/test/resources/test-model.txt";
    private static final int MIN_WORDS_FOR_EMBEDDING = 2;
    private static final double UNKNOWN_WORD_WEIGHT = 0.1;
    private static final double NOUN_WEIGHT = 1.2;
    private static final double VERB_WEIGHT = 1.1;
    private static final double ADJ_WEIGHT = 1.05;

    // Remove @Value annotations and use constants
    private int minWordsForEmbedding = MIN_WORDS_FOR_EMBEDDING;
    private double unknownWordWeight = UNKNOWN_WORD_WEIGHT;
    private double nounWeight = NOUN_WEIGHT;
    private double verbWeight = VERB_WEIGHT;
    private double adjWeight = ADJ_WEIGHT;

    @Autowired
    private Environment environment;
    
    @Autowired
    private ContextAwareEmbeddingService contextAwareEmbeddingService;

    public DocumentProcessingServiceImpl() {
        this.tika = new Tika();
        try {
            // Initialize OpenNLP components
            InputStream tokenModelIn = getClass().getResourceAsStream("/models/en-token.bin");
            InputStream posModelIn = getClass().getResourceAsStream("/models/en-pos-maxent.bin");

            if (tokenModelIn == null || posModelIn == null) {
                logger.warn("OpenNLP models not found in resources. Using basic tokenization.");
                this.tokenizer = null;
                this.posTagger = null;
            } else {
                this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                this.posTagger = new POSTaggerME(new POSModel(posModelIn));
                logger.info("Successfully initialized OpenNLP components");
            }

            // Initialize NLP pipeline
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
            this.pipeline = new StanfordCoreNLP(props);

            // Initialize tokenizer
            tokenizerFactory = new DefaultTokenizerFactory();
            // No preprocessor needed for simple test model

            // Load or create model
            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                logger.info("Loading existing model from: {}", MODEL_PATH);
                word2Vec = WordVectorSerializer.readWord2VecModel(modelFile);
            } else {
                logger.warn("Model file not found at: {}. Using small random model.", MODEL_PATH);
                // Create a small random model for testing
                this.word2Vec = new Word2Vec.Builder()
                        .minWordFrequency(1)
                        .iterations(1)
                        .layerSize(2)
                        .seed(42)
                        .windowSize(5)
                        .iterate(new BasicLineIterator(new File(MODEL_PATH)))
                        .tokenizerFactory(this.tokenizerFactory)
                        .build();
                this.word2Vec.fit();
            }
        } catch (Exception e) {
            logger.error("Error initializing models: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to initialize NLP models", e);
        }
    }

    private static class ProcessedWord {
        final String word;
        final String pos;
        final double weight;

        ProcessedWord(String word, String pos, double nounWeight, double verbWeight, double adjWeight) {
            this.word = word;
            this.pos = pos;
            this.weight = calculateWeight(pos, nounWeight, verbWeight, adjWeight);
        }

        private static double calculateWeight(String pos, double nounWeight, double verbWeight, double adjWeight) {
            if (pos.startsWith("NN")) return nounWeight;      // Nouns
            if (pos.startsWith("VB")) return verbWeight;      // Verbs
            if (pos.startsWith("JJ")) return adjWeight;       // Adjectives
            return 1.0;                                        // Other parts of speech
        }
    }

    private List<ProcessedWord> preprocessText(String text) {
        if (tokenizer == null || posTagger == null) {
            // Fallback to basic tokenization if OpenNLP models are not available
            return Arrays.stream(text.toLowerCase().split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> new ProcessedWord(word, "UNKNOWN", nounWeight, verbWeight, adjWeight))
                .collect(Collectors.toList());
        }

        try {
            // Tokenize
            String[] tokens = tokenizer.tokenize(text.toLowerCase());
            
            // Get POS tags
            String[] posTags = posTagger.tag(tokens);

            // Process each word with its POS tag
            List<ProcessedWord> processedWords = new ArrayList<>();
            for (int i = 0; i < tokens.length; i++) {
                if (!tokens[i].trim().isEmpty()) {
                    processedWords.add(new ProcessedWord(tokens[i], posTags[i], nounWeight, verbWeight, adjWeight));
                }
            }

            // Log POS statistics
            Map<String, Long> posStats = processedWords.stream()
                .collect(Collectors.groupingBy(w -> w.pos, Collectors.counting()));
            logger.debug("POS statistics: {}", posStats);

            return processedWords;
        } catch (Exception e) {
            logger.warn("Error in advanced text preprocessing, falling back to basic tokenization: {}", e.getMessage());
            return Arrays.stream(text.toLowerCase().split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> new ProcessedWord(word, "UNKNOWN", nounWeight, verbWeight, adjWeight))
                .collect(Collectors.toList());
        }
    }

    @Override
    public double[] generateEmbedding(String text) {
        if (StringUtils.isBlank(text)) {
            throw new DocumentProcessingException("Input text cannot be empty");
        }

        // Try context-aware embedding first if available
        if (contextAwareEmbeddingService != null && contextAwareEmbeddingService.isServiceAvailable()) {
            try {
                // Generate static embedding first
                double[] staticEmbedding = generateStaticEmbedding(text);
                
                // Generate hybrid embedding combining static and contextual
                double[] hybridEmbedding = contextAwareEmbeddingService.generateHybridEmbedding(text, staticEmbedding);
                
                logger.debug("Generated hybrid embedding using context-aware service");
                return hybridEmbedding;
                
            } catch (Exception e) {
                logger.warn("Context-aware embedding failed, falling back to static: {}", e.getMessage());
                return generateStaticEmbedding(text);
            }
        }
        
        // Fallback to static embedding
        return generateStaticEmbedding(text);
    }
    
    /**
     * Generate static embedding using GloVe/Word2Vec approach
     */
    public double[] generateStaticEmbedding(String text) {
        if (StringUtils.isBlank(text)) {
            throw new DocumentProcessingException("Input text cannot be empty");
        }

        List<ProcessedWord> processedWords = preprocessText(text);
        if (processedWords.size() < minWordsForEmbedding) {
            throw new DocumentProcessingException(
                String.format("Input text must contain at least %d words", minWordsForEmbedding));
        }

        // Track known and unknown words with their weights
        List<ProcessedWord> knownWords = new ArrayList<>();
        List<ProcessedWord> unknownWords = new ArrayList<>();
        
        for (ProcessedWord word : processedWords) {
            if (word2Vec.hasWord(word.word)) {
                knownWords.add(word);
            } else {
                unknownWords.add(word);
            }
        }

        // Log word statistics
        logger.debug("Text processing stats - Total words: {}, Known words: {}, Unknown words: {}", 
            processedWords.size(), knownWords.size(), unknownWords.size());

        if (knownWords.isEmpty()) {
            throw new DocumentProcessingException(
                "No known words found in the text. Please use different words or check the input.");
        }

        // Generate embedding by combining known and unknown word vectors
        double[] embedding = new double[word2Vec.getLayerSize()];
        double totalWeight = 0.0;

        // Process known words with their respective weights
        for (ProcessedWord word : knownWords) {
            double[] wordVector = word2Vec.getWordVector(word.word);
            for (int i = 0; i < wordVector.length; i++) {
                embedding[i] += wordVector[i] * word.weight;
            }
            totalWeight += word.weight;
        }

        // Process unknown words with reduced weight
        if (!unknownWords.isEmpty()) {
            Random random = new Random(42);
            for (ProcessedWord word : unknownWords) {
                double[] randomVector = new double[word2Vec.getLayerSize()];
                for (int i = 0; i < randomVector.length; i++) {
                    randomVector[i] = (random.nextDouble() * 2 - 1) * unknownWordWeight * word.weight;
                }
                for (int i = 0; i < randomVector.length; i++) {
                    embedding[i] += randomVector[i];
                }
                totalWeight += word.weight * unknownWordWeight;
            }
        }

        // Normalize the embedding
        if (totalWeight > 0) {
            double norm = 0.0;
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= totalWeight;
                norm += embedding[i] * embedding[i];
            }
            norm = Math.sqrt(norm);
            
            // Normalize to unit length
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }

        // Verify the embedding is not a zero vector
        boolean isZeroVector = true;
        for (double value : embedding) {
            if (Math.abs(value) > 1e-10) {
                isZeroVector = false;
                break;
            }
        }

        if (isZeroVector) {
            throw new DocumentProcessingException("Failed to generate a valid embedding. Please try different input text.");
        }

        return embedding;
    }

    @Override
    public String extractText(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            String text = tika.parseToString(bis);
            if (StringUtils.isBlank(text)) {
                throw new DocumentProcessingException("Extracted text is empty");
            }
            return text;
        } catch (IOException | TikaException e) {
            throw new DocumentProcessingException("Failed to extract text from document", e);
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