package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record SimilarityRequest(

        @NotBlank
        String textA,

        @NotBlank
        String textB

) {
}