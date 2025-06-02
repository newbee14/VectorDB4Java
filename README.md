# VectorDB4Java

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

## Indexing Strategy

The application uses Apache Lucene for vector similarity search, implementing a custom indexing strategy optimized for high-dimensional vectors.

### Current Implementation

- **Index Structure**: Uses Lucene's `ByteBuffersDirectory` for in-memory indexing
- **Vector Storage**: Vectors are stored as binary fields in Lucene documents
- **Similarity Search**: Implements K-Nearest Neighbors (KNN) search using cosine similarity
- **Performance Characteristics**:
  - Fast for small to medium-sized datasets (up to ~100K vectors)
  - Memory-efficient due to in-memory indexing
  - Linear search complexity (O(n) for n vectors)

### Limitations and Potential Improvements

1. **Scalability**:
   - Current implementation uses in-memory indexing, limiting dataset size
   - Could be improved by implementing disk-based indexing for larger datasets
   - Consider using Lucene's `MMapDirectory` or `NIOFSDirectory` for persistent storage

2. **Search Performance**:
   - Linear search becomes slow for large datasets
   - Potential improvements:
     - Implement HNSW (Hierarchical Navigable Small World) graph for approximate nearest neighbors
     - Use Product Quantization (PQ) for vector compression
     - Implement IVF (Inverted File) index for faster approximate search


### Relevant Resources

1. **Vector Search Fundamentals**:
   - [Approximate Nearest Neighbors Oh Yeah (ANNOY)](https://github.com/spotify/annoy) - Spotify's library for approximate nearest neighbors
   - [HNSW: Hierarchical Navigable Small World](https://arxiv.org/abs/1603.09320) - Paper on HNSW algorithm
   - [Product Quantization for Nearest Neighbor Search](https://lear.inrialpes.fr/pubs/2011/JDS11/jegou_searching_with_quantization.pdf) - Paper on PQ technique

2. **Lucene and Vector Search**:
   - [Apache Lucene Documentation](https://lucene.apache.org/core/documentation.html)
   - [Lucene Vector Search](https://lucene.apache.org/core/9_7_0/core/org/apache/lucene/search/VectorSimilarityQuery.html)
   - [Elasticsearch Vector Search](https://www.elastic.co/guide/en/elasticsearch/reference/current/vector-search.html) - Example of production-grade vector search implementation

3. **Alternative Solutions**:
   - [FAISS](https://github.com/facebookresearch/faiss) - Facebook's library for efficient similarity search
   - [Milvus](https://milvus.io/) - Open-source vector database
   - [Weaviate](https://weaviate.io/) - Vector search engine with GraphQL API

4. **Performance Optimization**:
   - [Vector Search Performance](https://www.pinecone.io/learn/vector-search-performance/) - Guide to vector search performance
   - [Approximate Nearest Neighbor Search](https://www.pinecone.io/learn/approximate-nearest-neighbor/) - Overview of ANN algorithms

## License

MIT License 