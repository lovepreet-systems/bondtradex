package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CancelIoiRequest(

        @Size(
                max = 1000,
                message = "Cancellation reason cannot exceed 1000 characters"
        )
        String reason,

        @NotNull(message = "Version is required")
        Long version
) {
}