package com.bondtradex.ioi.outbox.service;

import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.event.EventEnvelope;
import com.bondtradex.ioi.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(
            EventEnvelope<?> envelope,
            String topic,
            String eventKey
    ) {
        String payload = serialize(envelope);

        OutboxEvent outboxEvent = OutboxEvent.pending(
                envelope.eventId(),
                envelope.eventType(),
                envelope.aggregateType(),
                envelope.aggregateId(),
                topic,
                eventKey,
                payload,
                clock.instant()
        );

        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(EventEnvelope<?> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new OutboxSerializationException(
                    "Failed to serialize outbox event: "
                            + envelope.eventId(),
                    exception
            );
        }
    }
}