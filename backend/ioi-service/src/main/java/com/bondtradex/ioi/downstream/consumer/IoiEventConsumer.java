package com.bondtradex.ioi.downstream.consumer;

import com.bondtradex.ioi.downstream.entity.ProcessedKafkaEvent;
import com.bondtradex.ioi.downstream.repository.ProcessedKafkaEventRepository;
import com.bondtradex.ioi.downstream.service.IoiDownstreamProcessingService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IoiEventConsumer {

    private static final String CONSUMER_NAME =
            "ioi-downstream-consumer";

    private final ProcessedKafkaEventRepository repository;

    private final IoiDownstreamProcessingService downstreamService;

    @KafkaListener(
            topics = {
                    "${app.kafka.topics.ioi-events}",
                    "${app.kafka.topics.ioi-events-replay}"
            },
            groupId =
                    "ioi-downstream-consumer-group"
    )
    public void consume(ConsumerRecord<String, String> record)
    {

        UUID eventId = UUID.fromString(record.key());

        boolean alreadyProcessed =
                repository
                        .existsByEventIdAndConsumerName(
                                eventId,
                                CONSUMER_NAME
                        );

        if (alreadyProcessed) {

            log.info(
                    "Duplicate event skipped {}",
                    eventId
            );

            return;
        }

        downstreamService.process(record.value());

        ProcessedKafkaEvent event = new ProcessedKafkaEvent();

        event.setEventId(eventId);

        event.setConsumerName(CONSUMER_NAME);

        event.setSourceTopic(record.topic());

        event.setSourcePartition(record.partition());

        event.setSourceOffset(record.offset());

        event.setProcessedAt(Instant.now());

        repository.save(event);

        log.info(
                "Event successfully processed {}",
                eventId
        );
    }
}