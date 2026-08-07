package com.bondtradex.ioi.outbox.entity;

import com.bondtradex.ioi.outbox.status.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected OutboxEvent() {
    }

    public static OutboxEvent pending(
            UUID id,
            String eventType,
            String aggregateType,
            UUID aggregateId,
            String topic,
            String eventKey,
            String payload,
            Instant createdAt
    ) {
        OutboxEvent event = new OutboxEvent();

        event.id = id;
        event.eventType = eventType;
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.topic = topic;
        event.eventKey = eventKey;
        event.payload = payload;
        event.status = OutboxStatus.PENDING;
        event.retryCount = 0;
        event.createdAt = createdAt;
        event.updatedAt = createdAt;

        return event;
    }

    public void markProcessing(Instant processingAt) {
        this.status = OutboxStatus.PROCESSING;
        this.updatedAt = processingAt;
    }

    public void markPublished(Instant publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.updatedAt = publishedAt;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markFailed(
            String errorMessage,
            Instant failedAt,
            Instant nextRetryAt,
            int maximumRetries
    ) {
        this.retryCount++;
        this.lastError = truncateError(errorMessage);
        this.updatedAt = failedAt;

        if (hasReachedMaximumRetries(maximumRetries)) {
            markDead(failedAt);
            return;
        }

        this.status = OutboxStatus.FAILED;
        this.nextRetryAt = nextRetryAt;
    }

    private void markDead(Instant deadAt) {
        this.status = OutboxStatus.DEAD;
        this.nextRetryAt = null;
        this.updatedAt = deadAt;
    }

    public boolean hasReachedMaximumRetries(
            int maximumRetries
    ) {
        return retryCount >= maximumRetries;
    }

    private String truncateError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Unknown Kafka publishing error";
        }

        int maximumLength = 2000;

        if (errorMessage.length() <= maximumLength) {
            return errorMessage;
        }

        return errorMessage.substring(0, maximumLength);
    }

    public void recoverFromStaleProcessing(
            Instant recoveredAt,
            Instant nextRetryAt,
            int maximumRetries
    ) {
        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Only PROCESSING events can be recovered"
            );
        }

        markFailed(
                "Recovered stale PROCESSING outbox event",
                recoveredAt,
                nextRetryAt,
                maximumRetries
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getTopic() {
        return topic;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Long getVersion() {
        return version;
    }
}