package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotNull;

public record SubmitIoiRequest(

        @NotNull(message = "Version is required")
        Long version
) {
}