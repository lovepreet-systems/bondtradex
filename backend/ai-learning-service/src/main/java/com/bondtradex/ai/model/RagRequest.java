package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record RagRequest(

        @NotBlank
        String question

) {
}