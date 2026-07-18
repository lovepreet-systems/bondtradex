package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApproveIoiRequest(

        @NotNull(message = "Trader user ID is required")
        UUID traderUserId,

        @Size(
                max = 1000,
                message = "Trader comment cannot exceed 1000 characters"
        )
        String traderComment,

        @NotNull(message = "Version is required")
        Long version
) {
}