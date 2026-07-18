package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RejectIoiRequest(

        @NotNull(message = "Trader user ID is required")
        UUID traderUserId,

        @Size(
                max = 1000,
                message = "Trader comment cannot exceed 1000 characters"
        )
        String traderComment,

        @NotBlank(message = "Rejection reason is required")
        @Size(
                max = 1000,
                message = "Rejection reason cannot exceed 1000 characters"
        )
        String rejectionReason,

        @NotNull(message = "Version is required")
        Long version
) {
}