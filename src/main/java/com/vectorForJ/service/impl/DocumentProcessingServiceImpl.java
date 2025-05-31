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
import org.apache.commons.lang3.StringUtils;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingServiceImpl.class);
    private final Tika tika;
    private final Word2Vec word2Vec;
    private static final String MODEL_PATH = "models/glove.6B.100d.txt";
    private static final int MIN_WORDS_FOR_EMBEDDING = 3;
    private static final double UNKNOWN_WORD_WEIGHT = 0.1;
    private static final double NOUN_WEIGHT = 1.2; // Give more weight to nouns
    private static final double VERB_WEIGHT = 1.1; // Give more weight to verbs
    private static final double ADJ_WEIGHT = 1.05; // Give slightly more weight to adjectives

    // OpenNLP components
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;

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

            // Initialize Word2Vec
            File modelFile = new File(MODEL_PATH);
            if (!modelFile.exists()) {
                logger.warn("Pre-trained model not found at {}. Using a small random model instead.", MODEL_PATH);
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
            logger.error("Error initializing models: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to initialize NLP models", e);
        }
    }

    private static class ProcessedWord {
        final String word;
        final String pos;
        final double weight;

        ProcessedWord(String word, String pos) {
            this.word = word;
            this.pos = pos;
            this.weight = calculateWeight(pos);
        }

        private static double calculateWeight(String pos) {
            if (pos.startsWith("NN")) return NOUN_WEIGHT;      // Nouns
            if (pos.startsWith("VB")) return VERB_WEIGHT;      // Verbs
            if (pos.startsWith("JJ")) return ADJ_WEIGHT;       // Adjectives
            return 1.0;                                        // Other parts of speech
        }
    }

    private List<ProcessedWord> preprocessText(String text) {
        if (tokenizer == null || posTagger == null) {
            // Fallback to basic tokenization if OpenNLP models are not available
            return Arrays.stream(text.toLowerCase().split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> new ProcessedWord(word, "UNKNOWN"))
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
                    processedWords.add(new ProcessedWord(tokens[i], posTags[i]));
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
                .map(word -> new ProcessedWord(word, "UNKNOWN"))
                .collect(Collectors.toList());
        }
    }

    @Override
    public double[] generateEmbedding(String text) {
        if (StringUtils.isBlank(text)) {
            throw new DocumentProcessingException("Input text cannot be empty");
        }

        List<ProcessedWord> processedWords = preprocessText(text);
        if (processedWords.size() < MIN_WORDS_FOR_EMBEDDING) {
            throw new DocumentProcessingException(
                String.format("Input text must contain at least %d words", MIN_WORDS_FOR_EMBEDDING));
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
                    randomVector[i] = (random.nextDouble() * 2 - 1) * UNKNOWN_WORD_WEIGHT * word.weight;
                }
                for (int i = 0; i < randomVector.length; i++) {
                    embedding[i] += randomVector[i];
                }
                totalWeight += word.weight * UNKNOWN_WORD_WEIGHT;
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