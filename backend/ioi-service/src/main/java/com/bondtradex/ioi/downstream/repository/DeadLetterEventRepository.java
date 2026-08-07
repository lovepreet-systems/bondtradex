package com.bondtradex.ioi.downstream.repository;

import com.bondtradex.ioi.downstream.entity.DeadLetterEvent;
import com.bondtradex.ioi.downstream.model.ReplayStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterEventRepository
        extends JpaRepository<DeadLetterEvent, Long> {

    Optional<DeadLetterEvent> findByDeadLetterEventId(
            UUID deadLetterEventId
    );

    boolean existsByDeadLetterEventId(
            UUID deadLetterEventId
    );

    List<DeadLetterEvent> findByStatusOrderByFailedAtAsc(
            ReplayStatus status
    );

    List<DeadLetterEvent>
    findByCorrelationIdOrderByFailedAtDesc(
            String correlationId
    );

    List<DeadLetterEvent>
    findByOriginalEventIdOrderByFailedAtDesc(
            UUID originalEventId
    );

    List<DeadLetterEvent>
    findByStatusAndFailedAtBetweenOrderByFailedAtAsc(
            ReplayStatus status,
            Instant from,
            Instant to
    );
}