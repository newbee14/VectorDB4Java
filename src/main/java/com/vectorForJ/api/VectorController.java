package com.vectorForJ.api;

import com.vectorForJ.constants.ApplicationConstants.Api;
import com.vectorForJ.constants.ApplicationConstants.File;
import com.vectorForJ.constants.ApplicationConstants.Defaults;
import com.vectorForJ.model.Vector;
import com.vectorForJ.service.DocumentProcessingService;
import com.vectorForJ.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for vector operations and document processing.
 */
@RestController
@RequestMapping(Api.VECTORS_PATH)
@Validated
@Tag(name = Api.VECTOR_OPERATIONS_TAG, description = Api.VECTOR_OPERATIONS_DESC)
public class VectorController {

    private final VectorService vectorService;
    private final DocumentProcessingService documentProcessingService;

    @Autowired
    public VectorController(VectorService vectorService, DocumentProcessingService documentProcessingService) {
        this.vectorService = vectorService;
        this.documentProcessingService = documentProcessingService;
    }

    /**
     * Processes a document file and creates a vector from its content.
     */
    @Operation(summary = "Process a document file", description = "Uploads a document, extracts text, and creates a vector embedding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or processing error")
    })
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Vector> processDocument(
            @Parameter(description = "Document file to process")
            @RequestParam(File.FILE_PARAM) MultipartFile file) {
        Vector vector = documentProcessingService.processDocument(file);
        return ResponseEntity.ok(vectorService.createVector(vector));
    }

    /**
     * Creates a vector from raw text input.
     */
    @Operation(summary = "Process text input", description = "Creates a vector embedding from raw text")
    @GetMapping("/text")
    public ResponseEntity<Vector> processText(
            @Parameter(description = "Text to process")
            @RequestParam(File.TEXT_PARAM) @Valid String text) {
        double[] embedding = documentProcessingService.generateEmbedding(text);
        Vector vector = new Vector(null, embedding, File.TEXT_INPUT_TYPE, embedding.length);
        return ResponseEntity.ok(vectorService.createVector(vector));
    }

    /**
     * Creates a new vector directly from vector data.
     */
    @Operation(summary = "Create a vector", description = "Creates a new vector directly from vector data")
    @PostMapping
    public ResponseEntity<Vector> createVector(
            @Parameter(description = "Vector data") 
            @Valid @RequestBody Vector vector) {
        return ResponseEntity.ok(vectorService.createVector(vector));
    }

    /**
     * Retrieves a vector by its ID.
     */
    @Operation(summary = "Get a vector by ID", description = "Retrieves a vector by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vector found"),
        @ApiResponse(responseCode = "404", description = "Vector not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Vector> getVector(
            @Parameter(description = "Vector ID") 
            @PathVariable String id) {
        return vectorService.getVector(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all vectors in the database.
     */
    @Operation(summary = "Get all vectors", description = "Retrieves all vectors from the database")
    @GetMapping
    public ResponseEntity<List<Vector>> getAllVectors() {
        return ResponseEntity.ok(vectorService.getAllVectors());
    }

    /**
     * Deletes a vector by its ID.
     */
    @Operation(summary = "Delete a vector", description = "Deletes a vector by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVector(
            @Parameter(description = "Vector ID") 
            @PathVariable String id) {
        vectorService.deleteVector(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Finds similar vectors using cosine similarity.
     * @param k Number of similar vectors to return
     */
    @Operation(summary = "Find similar vectors", description = "Finds k most similar vectors to the query vector")
    @PostMapping("/search")
    public ResponseEntity<List<Vector>> findSimilarVectors(
            @Parameter(description = "Query vector")
            @RequestBody double[] queryVector,
            @Parameter(description = "Number of similar vectors to return")
            @RequestParam(defaultValue = Defaults.DEFAULT_K_VALUE) @Min(1) int k) {
        return ResponseEntity.ok(vectorService.findSimilarVectors(queryVector, k));
    }

    /**
     * Returns the total count of vectors in the database.
     */
    @Operation(summary = "Get vector count", description = "Returns the total number of vectors in the database")
    @GetMapping("/count")
    public ResponseEntity<Integer> getVectorCount() {
        return ResponseEntity.ok(vectorService.getVectorCount());
    }
} 