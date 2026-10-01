package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(

        @NotBlank
        String content,

        String technology,

        String service,

        String environment
) {
}