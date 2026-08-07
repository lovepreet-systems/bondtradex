package com.bondtradex.ioi.downstream.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplayRequest(

        @NotBlank
        String reason
) {
}
