package com.bondtradex.ioi.downstream.consumer;

import com.bondtradex.ioi.downstream.entity.DeadLetterEvent;
import com.bondtradex.ioi.downstream.model.DeadLetterEventMessage;
import com.bondtradex.ioi.downstream.repository.DeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterEventConsumer {

    private final DeadLetterEventRepository repository;

    @Transactional
    @KafkaListener(
            topics = "${app.kafka.topics.ioi-events-dlt}",
            groupId = "ioi-dlt-persistence-group"
    )
    public void consume(
            ConsumerRecord<String, DeadLetterEventMessage> record
    ) {
        DeadLetterEventMessage message = record.value();

        if (repository.existsByDeadLetterEventId(message.deadLetterEventId())) {
            log.info(
                    "Duplicate DLT message skipped. "
                            + "deadLetterEventId={}",
                    message.deadLetterEventId()
            );

            return;
        }

        DeadLetterEvent entity =
                DeadLetterEvent.from(message);

        repository.save(entity);

        log.error(
                "Dead-letter event persisted. "
                        + "deadLetterEventId={}, "
                        + "originalEventId={}, "
                        + "originalTopic={}, "
                        + "originalPartition={}, "
                        + "originalOffset={}, "
                        + "errorCategory={}",
                message.deadLetterEventId(),
                message.originalEventId(),
                message.originalTopic(),
                message.originalPartition(),
                message.originalOffset(),
                message.errorCategory()
        );
    }
}