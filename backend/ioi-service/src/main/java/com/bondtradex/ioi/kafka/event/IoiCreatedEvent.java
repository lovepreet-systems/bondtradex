package com.bondtradex.ioi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record IoiCreatedEvent(
        UUID eventId,
        UUID ioiId,
        String isin,
        String status,
        String clientId,
        Instant occurredAt
) {
}