package com.bondtradex.ioi.dto;

import com.bondtradex.ioi.entity.IoiSide;
import com.bondtradex.ioi.entity.IoiStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record IoiResponse(
        UUID id,
        String ioiNumber,
        UUID clientId,
        UUID instrumentId,
        String isin,
        String cusip,
        IoiSide side,
        BigDecimal quantity,
        BigDecimal targetPrice,
        String currency,
        LocalDate settlementDate,
        IoiStatus status,
        String clientComment,
        UUID salesUserId,
        String salesComment,
        UUID traderUserId,
        String traderComment,
        String rejectionReason,
        UUID offeringId,
        Instant submittedAt,
        Instant salesReviewedAt,
        Instant traderReviewedAt,
        Instant approvedAt,
        Instant offeringCreatedAt,
        Instant createdAt,
        String createdBy,
        Long version
) {
}