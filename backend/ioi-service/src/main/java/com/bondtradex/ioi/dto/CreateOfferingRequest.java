package com.bondtradex.ioi.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOfferingRequest(

        @NotNull(message = "Offering ID is required")
        UUID offeringId,

        @NotNull(message = "Version is required")
        Long version

) {
}