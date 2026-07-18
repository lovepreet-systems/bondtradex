package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteSalesReviewRequest(

        @NotBlank(message = "Sales comment is required")
        @Size(
                max = 1000,
                message = "Sales comment cannot exceed 1000 characters"
        )
        String salesComment,

        @NotNull(message = "Version is required")
        Long version
) {
}
