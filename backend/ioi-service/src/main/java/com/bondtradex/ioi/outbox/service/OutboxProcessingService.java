package com.bondtradex.ioi.outbox.service;

import com.bondtradex.ioi.outbox.config.OutboxProperties;
import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.repository.OutboxEventRepository;
import com.bondtradex.ioi.outbox.status.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxProcessingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OutboxProcessingService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxProperties outboxProperties;
    private final Clock clock;

    public OutboxProcessingService(
            OutboxEventRepository outboxEventRepository,
            OutboxProperties outboxProperties,
            Clock clock
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxProperties = outboxProperties;
        this.clock = clock;
    }

    @Transactional
    public List<OutboxEvent> claimPublishableEvents() {
        Instant now = clock.instant();

        List<OutboxEvent> events =
                outboxEventRepository.findPublishableEvents(
                        OutboxStatus.PENDING,
                        OutboxStatus.FAILED,
                        now,
                        PageRequest.of(
                                0,
                                outboxProperties.getBatchSize()
                        )
                );

        events.forEach(event ->
                event.markProcessing(now)
        );

        outboxEventRepository.flush();

        return events;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(UUID eventId) {
        OutboxEvent event = findById(eventId);

        event.markPublished(clock.instant());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboxStatus markFailed(
            UUID eventId,
            String errorMessage
    ) {
        OutboxEvent event = findById(eventId);

        Instant failedAt = clock.instant();

        Instant nextRetryAt = failedAt.plusSeconds(
                outboxProperties.getRetryDelaySeconds()
        );

        event.markFailed(
                errorMessage,
                failedAt,
                nextRetryAt,
                outboxProperties.getMaxRetries()
        );

        if (event.getStatus() == OutboxStatus.DEAD) {
            LOGGER.error(
                    "Outbox event reached maximum retries and is now DEAD. "
                            + "eventId={}, eventType={}, retryCount={}",
                    event.getId(),
                    event.getEventType(),
                    event.getRetryCount()
            );
        }
        event.markFailed(
                errorMessage,
                Instant.now(),
                nextRetryAt,
                outboxProperties.getMaxRetries()
        );

        return event.getStatus();
    }

    private OutboxEvent findById(UUID eventId) {
        return outboxEventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Outbox event not found: "
                                        + eventId
                        )
                );
    }
}