package com.bondtradex.ioi.downstream.dto;

import java.time.Instant;
import java.util.UUID;

public record ReplayResponse(
        UUID deadLetterEventId,
        UUID originalEventId,
        String status,
        Instant replayedAt
) {
}
