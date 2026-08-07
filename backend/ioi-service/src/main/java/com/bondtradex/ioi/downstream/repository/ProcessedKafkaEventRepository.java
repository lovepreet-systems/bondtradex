package com.bondtradex.ioi.downstream.repository;

import com.bondtradex.ioi.downstream.entity.ProcessedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedKafkaEventRepository
        extends JpaRepository<ProcessedKafkaEvent, Long> {

    boolean existsByEventIdAndConsumerName(
            UUID eventId,
            String consumerName
    );
}
