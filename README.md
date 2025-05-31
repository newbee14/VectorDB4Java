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

## License

MIT License 