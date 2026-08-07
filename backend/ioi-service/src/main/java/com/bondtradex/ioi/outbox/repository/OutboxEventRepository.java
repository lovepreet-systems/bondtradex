package com.bondtradex.ioi.outbox.repository;

import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.status.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.status = :pendingStatus
               OR (
                    event.status = :failedStatus
                    AND event.nextRetryAt <= :now
               )
            ORDER BY event.createdAt ASC
            """)
    List<OutboxEvent> findPublishableEvents(
            @Param("pendingStatus")
            OutboxStatus pendingStatus,

            @Param("failedStatus")
            OutboxStatus failedStatus,

            @Param("now")
            Instant now,

            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.status = :processingStatus
              AND event.updatedAt <= :cutoffTime
            ORDER BY event.updatedAt ASC
            """)
    List<OutboxEvent> findStaleProcessingEvents(
            @Param("processingStatus")
            OutboxStatus processingStatus,

            @Param("cutoffTime")
            Instant cutoffTime,

            Pageable pageable
    );

    long countByStatus(OutboxStatus status);
}