package com.bondtradex.ioi.outbox.service;

import com.bondtradex.ioi.outbox.config.OutboxProperties;
import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.metrics.OutboxMetrics;
import com.bondtradex.ioi.outbox.repository.OutboxEventRepository;
import com.bondtradex.ioi.outbox.status.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class OutboxRecoveryService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OutboxRecoveryService.class
            );

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxProperties outboxProperties;
    private final Clock clock;
    private final OutboxMetrics outboxMetrics;

    public OutboxRecoveryService(
            OutboxEventRepository outboxEventRepository,
            OutboxProperties outboxProperties,
            Clock clock,
            OutboxMetrics outboxMetrics
    ) {
        this.outboxEventRepository =
                outboxEventRepository;
        this.outboxProperties =
                outboxProperties;
        this.clock = clock;
        this.outboxMetrics=outboxMetrics;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.outbox.publisher.recovery-delay-ms}"
    )
    @Transactional
    public void recoverStaleProcessingEvents() {
        Instant now = clock.instant();

        Instant cutoffTime = now.minusSeconds(
                outboxProperties
                        .getProcessingTimeoutSeconds()
        );

        List<OutboxEvent> staleEvents =
                outboxEventRepository
                        .findStaleProcessingEvents(
                                OutboxStatus.PROCESSING,
                                cutoffTime,
                                PageRequest.of(
                                        0,
                                        outboxProperties
                                                .getBatchSize()
                                )
                        );

        if (staleEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : staleEvents) {
            Instant nextRetryAt = now.plusSeconds(
                    outboxProperties
                            .getRetryDelaySeconds()
            );

            event.recoverFromStaleProcessing(
                    now,
                    nextRetryAt,
                    outboxProperties.getMaxRetries()
            );
            outboxMetrics.recordRecovered(
                    event.getStatus()
            );
            LOGGER.warn(
                    "Recovered stale PROCESSING outbox event. "
                            + "eventId={}, eventType={}, "
                            + "status={}, retryCount={}",
                    event.getId(),
                    event.getEventType(),
                    event.getStatus(),
                    event.getRetryCount()
            );
        }

        outboxEventRepository.flush();
    }
}