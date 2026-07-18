package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotNull;

public record StartSalesReviewRequest(

        @NotNull(message = "Version is required")
        Long version

) {
}