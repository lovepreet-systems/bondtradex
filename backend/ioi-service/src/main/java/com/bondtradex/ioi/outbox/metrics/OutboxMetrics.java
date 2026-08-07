package com.bondtradex.ioi.outbox.metrics;

import com.bondtradex.ioi.outbox.status.OutboxStatus;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import com.bondtradex.ioi.outbox.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxMetrics {

    private final Counter publishedCounter;
    private final Counter failedCounter;
    private final Counter deadCounter;
    private final Counter recoveredCounter;

    private final Timer publishingTimer;
    private final DistributionSummary batchSizeSummary;

    public OutboxMetrics(
            MeterRegistry meterRegistry,
            OutboxEventRepository outboxEventRepository
    ){
        this.publishedCounter = Counter.builder(
                        "outbox.events.published"
                )
                .description(
                        "Number of outbox events successfully published"
                )
                .register(meterRegistry);

        this.failedCounter = Counter.builder(
                        "outbox.events.failed"
                )
                .description(
                        "Number of outbox event publishing failures"
                )
                .register(meterRegistry);

        this.deadCounter = Counter.builder(
                        "outbox.events.dead"
                )
                .description(
                        "Number of outbox events moved to DEAD status"
                )
                .register(meterRegistry);

        this.recoveredCounter = Counter.builder(
                        "outbox.events.recovered"
                )
                .description(
                        "Number of stale PROCESSING events recovered"
                )
                .register(meterRegistry);

        this.publishingTimer = Timer.builder(
                        "outbox.publish.duration"
                )
                .description(
                        "Time taken to publish an outbox event"
                )
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.batchSizeSummary = DistributionSummary.builder(
                        "outbox.publish.batch.size"
                )
                .description(
                        "Number of outbox events claimed per publisher run"
                )
                .baseUnit("events")
                .register(meterRegistry);

        registerStatusGauge(
                meterRegistry,
                outboxEventRepository,
                "outbox.events.pending.current",
                "Current number of pending outbox events",
                OutboxStatus.PENDING
        );

        registerStatusGauge(
                meterRegistry,
                outboxEventRepository,
                "outbox.events.published.current",
                "Current number of Published outbox events",
                OutboxStatus.PUBLISHED
        );

        registerStatusGauge(
                meterRegistry,
                outboxEventRepository,
                "outbox.events.processing.current",
                "Current number of processing outbox events",
                OutboxStatus.PROCESSING
        );

        registerStatusGauge(
                meterRegistry,
                outboxEventRepository,
                "outbox.events.failed.current",
                "Current number of failed outbox events",
                OutboxStatus.FAILED
        );

        registerStatusGauge(
                meterRegistry,
                outboxEventRepository,
                "outbox.events.dead.current",
                "Current number of dead outbox events",
                OutboxStatus.DEAD
        );


    }

    private void registerStatusGauge(
            MeterRegistry meterRegistry,
            OutboxEventRepository repository,
            String metricName,
            String description,
            OutboxStatus status
    ) {
        Gauge.builder(
                        metricName,
                        repository,
                        currentRepository ->
                                currentRepository.countByStatus(status)
                )
                .description(description)
                .baseUnit("events")
                .register(meterRegistry);
    }

    public void recordPublished() {
        publishedCounter.increment();
    }

    public void recordFailure(OutboxStatus resultingStatus) {
        failedCounter.increment();

        if (resultingStatus == OutboxStatus.DEAD) {
            deadCounter.increment();
        }
    }

    public void recordRecovered(OutboxStatus resultingStatus) {
        recoveredCounter.increment();

        if (resultingStatus == OutboxStatus.DEAD) {
            deadCounter.increment();
        }
    }

    public void recordBatchSize(int batchSize) {
        batchSizeSummary.record(batchSize);
    }

    public void recordPublishingDuration(long durationNanos) {
        publishingTimer.record(
                durationNanos,
                TimeUnit.NANOSECONDS
        );
    }
}