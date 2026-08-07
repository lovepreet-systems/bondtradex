package com.bondtradex.ioi.outbox.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(

        UUID eventId,

        String eventType,

        String aggregateType,

        UUID aggregateId,

        Instant occurredAt,

        String correlationId,

        int eventVersion,

        T payload
) {

    public EventEnvelope {
        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID cannot be null"
            );
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "Event type cannot be blank"
            );
        }

        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException(
                    "Aggregate type cannot be blank"
            );
        }

        if (aggregateId == null) {
            throw new IllegalArgumentException(
                    "Aggregate ID cannot be null"
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Occurred-at timestamp cannot be null"
            );
        }

        if (eventVersion <= 0) {
            throw new IllegalArgumentException(
                    "Event version must be greater than zero"
            );
        }

        if (payload == null) {
            throw new IllegalArgumentException(
                    "Event payload cannot be null"
            );
        }
    }

    public static <T> EventEnvelope<T> create(
            String eventType,
            String aggregateType,
            UUID aggregateId,
            String correlationId,
            int eventVersion,
            T payload,
            Instant occurredAt
    ) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                occurredAt,
                correlationId,
                eventVersion,
                payload
        );
    }
}
