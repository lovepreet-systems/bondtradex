package com.bondtradex.ioi.downstream.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "processed_kafka_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_event",
                        columnNames = {
                                "event_id",
                                "consumer_name"
                        }
                )
        }
)
public class ProcessedKafkaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "source_topic")
    private String sourceTopic;

    @Column(name = "source_partition")
    private Integer sourcePartition;

    @Column(name = "source_offset")
    private Long sourceOffset;

    @Column(name = "processed_at")
    private Instant processedAt;
}
