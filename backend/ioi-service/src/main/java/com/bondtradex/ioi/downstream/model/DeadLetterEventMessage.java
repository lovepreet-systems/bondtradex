package com.bondtradex.ioi.downstream.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record DeadLetterEventMessage(
        UUID deadLetterEventId,
        UUID originalEventId,
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        String originalKey,
        JsonNode originalPayload,
        String correlationId,
        String traceId,
        String consumerGroup,
        ErrorCategory errorCategory,
        String failureReason,
        String exceptionType,
        Integer attemptCount,
        Instant failedAt,
        String failedService
) {
}