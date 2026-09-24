package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeRequest(

        @NotBlank
        String text

) {
}
