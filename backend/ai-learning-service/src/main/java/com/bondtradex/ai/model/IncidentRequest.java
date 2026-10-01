package com.bondtradex.ai.model;

import jakarta.validation.constraints.NotBlank;

public record IncidentRequest(

        @NotBlank
        String incidentId,

        @NotBlank
        String service,

        @NotBlank
        String content

) {
}