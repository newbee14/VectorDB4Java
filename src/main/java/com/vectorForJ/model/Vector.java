package com.vectorForJ.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vectorForJ.constants.ApplicationConstants.Validation;

/**
 * Represents a vector in the vector database with its embedding and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vector {
    /** Unique identifier for the vector */
    private String id;

    /** Vector embedding values */
    @NotNull(message = Validation.EMBEDDING_NOT_NULL)
    private double[] embedding;

    /** Additional metadata about the vector */
    private String metadata;

    /** Dimension of the vector */
    @Positive(message = Validation.DIMENSION_POSITIVE)
    private int dimension;
} 