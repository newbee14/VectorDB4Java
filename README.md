# VectorForJ

A Spring Boot application for vector similarity search with advanced text processing capabilities.

## Key Features

- **Vector Similarity Search**: Fast KNN search using Apache Lucene
- **Smart Text Processing**:
  - Proper noun and compound word handling
  - Part-of-speech aware weighting (nouns: 1.2x, verbs: 1.1x, adjectives: 1.05x)
  - Automatic duplicate detection (95% similarity threshold)
- **Document Processing**: Extract and process text from various document formats
- **REST API**: Swagger UI available at `/swagger-ui.html`

## Quick Start

1. Build: `mvn clean install`
2. Run: `mvn spring-boot:run`
3. Access: `http://localhost:8080`

## API Examples

### Create Vector from Text
```bash
curl -X GET "http://localhost:8080/api/vectors/text?text=your text here"
```

### Find Similar Vectors
```bash
curl -X POST "http://localhost:8080/api/vectors/search" \
  -H "Content-Type: application/json" \
  -d '{"vector": [0.1, 0.2, ...]}'
```

### Process Document
```bash
curl -X POST "http://localhost:8080/api/vectors/document" \
  -F "file=@/path/to/document.pdf"
```

## Configuration

Key settings in `application.properties`:
```properties
# Vector similarity threshold (0.0 to 1.0)
vector.similarity.threshold=0.95

# File upload limits
spring.servlet.multipart.max-file-size=10MB
```

## Notes on Proper Noun Handling

- The application includes logic to handle proper nouns and part-of-speech-aware weighting during text processing.
- However, the effectiveness of proper noun distinction depends on the underlying embedding model (e.g., GloVe).
- In some cases, the model may not produce sufficiently distinct vectors for sentences differing only in proper nouns.
- The integration test for proper noun handling is currently ignored due to this limitation.

## Embedding Model

This project uses the [GloVe](https://nlp.stanford.edu/projects/glove/) (Global Vectors for Word Representation) pre-trained word embeddings for generating vector representations of text.

- **Model Used:** GloVe 6B, 100-dimensional vectors
- **Download Link:** [glove.6B.zip (822 MB)](https://nlp.stanford.edu/data/glove.6B.zip)
- **Direct File:** [glove.6B.100d.txt (347 MB)](https://nlp.stanford.edu/data/glove.6B.100d.txt)

### Setup Instructions
1. Download the GloVe model from the links above.
2. Unzip the archive if you downloaded `glove.6B.zip`.
3. Place the file `glove.6B.100d.txt` in the directory: `src/main/resources/models/`
   - The final path should be: `src/main/resources/models/glove.6B.100d.txt`

The application will automatically load this file at startup.

## License

MIT License 