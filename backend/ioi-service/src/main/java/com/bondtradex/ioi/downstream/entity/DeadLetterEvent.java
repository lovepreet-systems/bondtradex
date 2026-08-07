package com.bondtradex.ioi.downstream.entity;

import com.bondtradex.ioi.downstream.model.DeadLetterEventMessage;
import com.bondtradex.ioi.downstream.model.ErrorCategory;
import com.bondtradex.ioi.downstream.model.ReplayStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dead_letter_events")
public class DeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "dead_letter_event_id",
            nullable = false,
            unique = true
    )
    private UUID deadLetterEventId;

    @Column(
            name = "original_event_id",
            nullable = false
    )
    private UUID originalEventId;

    @Column(
            name = "original_topic",
            nullable = false,
            length = 255
    )
    private String originalTopic;

    @Column(name = "original_partition")
    private Integer originalPartition;

    @Column(name = "original_offset")
    private Long originalOffset;

    @Column(
            name = "original_key",
            length = 255
    )
    private String originalKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "original_payload",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private JsonNode originalPayload;

    @Column(
            name = "correlation_id",
            length = 255
    )
    private String correlationId;

    @Column(
            name = "trace_id",
            length = 255
    )
    private String traceId;

    @Column(
            name = "consumer_group",
            length = 255
    )
    private String consumerGroup;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "error_category",
            nullable = false,
            length = 100
    )
    private ErrorCategory errorCategory;

    @Column(
            name = "failure_reason",
            columnDefinition = "TEXT"
    )
    private String failureReason;

    @Column(
            name = "exception_type",
            length = 255
    )
    private String exceptionType;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private Integer attemptCount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private ReplayStatus status;

    @Column(
            name = "replay_count",
            nullable = false
    )
    private Integer replayCount;

    @Column(
            name = "failed_at",
            nullable = false
    )
    private Instant failedAt;

    @Column(name = "last_replayed_at")
    private Instant lastReplayedAt;



    protected DeadLetterEvent() {
        // Required by JPA.
    }

    public static DeadLetterEvent from(
            DeadLetterEventMessage message
    ) {
        DeadLetterEvent event = new DeadLetterEvent();

        event.deadLetterEventId =
                message.deadLetterEventId();

        event.originalEventId =
                message.originalEventId();

        event.originalTopic =
                message.originalTopic();

        event.originalPartition =
                message.originalPartition();

        event.originalOffset =
                message.originalOffset();

        event.originalKey =
                message.originalKey();

        event.originalPayload =
                message.originalPayload();

        event.correlationId =
                message.correlationId();

        event.traceId =
                message.traceId();

        event.consumerGroup =
                message.consumerGroup();

        event.errorCategory =
                message.errorCategory();

        event.failureReason =
                message.failureReason();

        event.exceptionType =
                message.exceptionType();

        event.attemptCount =
                message.attemptCount();

        event.failedAt =
                message.failedAt();

        event.status = ReplayStatus.FAILED;
        event.replayCount = 0;

        return event;
    }

    public void markReplayRequested(Instant replayedAt) {
        if (replayedAt == null) {
            throw new IllegalArgumentException(
                    "Replay time cannot be null"
            );
        }

        this.status = ReplayStatus.PENDING;
        this.replayCount++;
        this.lastReplayedAt = replayedAt;
    }

    public void markReplayRequested(String reason) {

        this.status = ReplayStatus.PENDING;

        this.replayCount = this.replayCount + 1;

        this.lastReplayedAt = Instant.now();
    }


    public void markReplayed() {
        this.status = ReplayStatus.REPLAYED;
        this.lastReplayedAt = Instant.now();
    }

    public void markReplayFailed() {
        this.status = ReplayStatus.FAILED;
    }

    public Long getId() {
        return id;
    }

    public UUID getDeadLetterEventId() {
        return deadLetterEventId;
    }

    public UUID getOriginalEventId() {
        return originalEventId;
    }

    public String getOriginalTopic() {
        return originalTopic;
    }

    public Integer getOriginalPartition() {
        return originalPartition;
    }

    public Long getOriginalOffset() {
        return originalOffset;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public JsonNode getOriginalPayload() {
        return originalPayload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public ErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public ReplayStatus getStatus() {
        return status;
    }

    public Integer getReplayCount() {
        return replayCount;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public Instant getLastReplayedAt() {
        return lastReplayedAt;
    }

    public void setLastReplayedAt(Instant lastReplayedAt) {
        this.lastReplayedAt = lastReplayedAt;
    }

    public void setReplayCount(Integer replayCount) {
        this.replayCount = replayCount;
    }

    public void setStatus(ReplayStatus status) {
        this.status = status;
    }


}