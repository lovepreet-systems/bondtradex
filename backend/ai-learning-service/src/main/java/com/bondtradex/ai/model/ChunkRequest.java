package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record ChunkRequest(

        @NotBlank
        String text,

        int chunkSize,

        int overlap

) {
}