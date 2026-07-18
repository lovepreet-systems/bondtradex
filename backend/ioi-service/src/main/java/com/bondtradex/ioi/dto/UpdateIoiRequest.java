package com.bondtradex.ioi.dto;

import com.bondtradex.ioi.entity.IoiSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateIoiRequest(

        @NotNull(message = "Client ID is required")
        UUID clientId,

        @NotNull(message = "Instrument ID is required")
        UUID instrumentId,

        @Pattern(
                regexp = "^[A-Z]{2}[A-Z0-9]{9}[0-9]$",
                message = "ISIN must be a valid 12-character identifier"
        )
        String isin,

        @Pattern(
                regexp = "^[A-Z0-9]{9}$",
                message = "CUSIP must contain exactly 9 alphanumeric characters"
        )
        String cusip,

        @NotNull(message = "Side is required")
        IoiSide side,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.0001",
                message = "Quantity must be greater than zero"
        )
        @Digits(
                integer = 15,
                fraction = 4,
                message = "Quantity format is invalid"
        )
        BigDecimal quantity,

        @DecimalMin(
                value = "0.0000",
                message = "Target price cannot be negative"
        )
        @Digits(
                integer = 15,
                fraction = 4,
                message = "Target price format is invalid"
        )
        BigDecimal targetPrice,

        @NotBlank(message = "Currency is required")
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency must be a three-letter uppercase code"
        )
        String currency,

        @FutureOrPresent(
                message = "Settlement date cannot be in the past"
        )
        LocalDate settlementDate,

        @Size(
                max = 1000,
                message = "Client comment cannot exceed 1000 characters"
        )
        String clientComment,

        @NotNull(message = "Version is required")
        Long version
) {
}