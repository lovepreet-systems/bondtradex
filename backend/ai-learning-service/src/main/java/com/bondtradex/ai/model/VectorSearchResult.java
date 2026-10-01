package com.bondtradex.ai.model;

public record VectorSearchResult(
        Long id,
        String content,
        double similarity
) {
}