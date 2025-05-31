# VectorForJ

VectorForJ is a powerful Java-based vector database and similarity search engine built with Spring Boot. It provides efficient storage, retrieval, and similarity search capabilities for high-dimensional vectors, making it ideal for applications involving embeddings, semantic search, and machine learning.

## Features

- **Vector Storage**: Store and manage high-dimensional vectors efficiently
- **Similarity Search**: Find nearest neighbors using Lucene's KNN search capabilities
- **Document Processing**: Process text documents and generate vector embeddings
- **RESTful API**: Easy-to-use HTTP endpoints for all operations
- **Swagger Documentation**: Interactive API documentation
- **Health Monitoring**: Built-in health check endpoints
- **Scalable**: Designed for both small and large-scale deployments

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- At least 4GB RAM (recommended for optimal performance)

## Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/vectorForJ.git
   cd vectorForJ
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port 8080. Access the Swagger UI at http://localhost:8080/swagger-ui.html

## API Endpoints

### Vector Operations

- `POST /api/vectors` - Create a new vector
- `GET /api/vectors/{id}` - Retrieve a vector by ID
- `GET /api/vectors` - List all vectors
- `GET /api/vectors/search` - Find nearest neighbors
- `DELETE /api/vectors/{id}` - Delete a vector

### Document Processing

- `POST /api/vectors/document` - Process a document and create a vector
- `GET /api/vectors/text` - Process text and create a vector

### Health Check

- `GET /actuator/health` - Check application health
- `GET /actuator/info` - Get application information

## Example Usage

### Creating a Vector

```bash
curl -X POST http://localhost:8080/api/vectors \
  -H "Content-Type: application/json" \
  -d '{
    "embedding": [0.1, 0.2, 0.3, ...],
    "metadata": "Sample vector"
  }'
```

### Finding Nearest Neighbors

```bash
curl -X GET "http://localhost:8080/api/vectors/search?vector=[0.1,0.2,0.3,...]&k=5"
```

### Processing a Document

```bash
curl -X POST http://localhost:8080/api/vectors/document \
  -F "file=@/path/to/document.pdf"
```

## Architecture

The application uses several key technologies:

- **Spring Boot**: Application framework
- **Apache Lucene**: Vector similarity search
- **DL4J**: Deep learning for embeddings
- **Apache Tika**: Document processing
- **OpenNLP**: Natural language processing

### Key Components

- `VectorIndexManager`: Manages vector storage and search using Lucene
- `DocumentProcessingService`: Handles document parsing and embedding generation
- `VectorService`: Business logic for vector operations
- `VectorController`: REST API endpoints

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server configuration
server.port=8080

# File upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.com.vectorForJ=DEBUG
```

## Development

### Building from Source

1. Clone the repository
2. Install dependencies:
   ```bash
   mvn dependency:resolve
   ```
3. Build:
   ```bash
   mvn clean install
   ```

### Running Tests

```bash
mvn test
```

## Performance Considerations

- The application uses Lucene's KNN search for efficient similarity search
- Vector dimensions are configurable (default: 1536)
- Memory usage scales with the number of vectors stored
- For large-scale deployments, consider:
  - Increasing JVM heap size
  - Using a persistent storage backend
  - Implementing caching strategies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Apache Lucene for vector search capabilities
- Deeplearning4j for embedding generation
- Spring Boot team for the excellent framework
- All contributors and users of the project

## Support

For issues, feature requests, or questions, please:
1. Check the existing issues
2. Create a new issue if needed
3. Provide detailed information about your use case

## Roadmap

- [ ] Add support for persistent storage
- [ ] Implement vector clustering
- [ ] Add more embedding models
- [ ] Support for batch operations
- [ ] Performance optimizations
- [ ] Additional similarity metrics 