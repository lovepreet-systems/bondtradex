package com.bondtradex.ioi.outbox;

import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.event.EventEnvelope;
import com.bondtradex.ioi.outbox.repository.OutboxEventRepository;
import com.bondtradex.ioi.outbox.service.OutboxService;
import com.bondtradex.ioi.outbox.status.OutboxStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxServiceTest {

    private OutboxEventRepository outboxEventRepository;
    private OutboxService outboxService;

    private final Instant fixedTime =
            Instant.parse("2026-07-25T20:00:00Z");

    @BeforeEach
    void setUp() {
        outboxEventRepository =
                mock(OutboxEventRepository.class);

        ObjectMapper objectMapper =
                new ObjectMapper().findAndRegisterModules();

        Clock clock = Clock.fixed(
                fixedTime,
                ZoneOffset.UTC
        );

        outboxService = new OutboxService(
                outboxEventRepository,
                objectMapper,
                clock
        );
    }

    @Test
    void shouldSavePendingOutboxEvent() {
        UUID aggregateId = UUID.randomUUID();

        EventEnvelope<Map<String, Object>> envelope =
                new EventEnvelope<>(
                        UUID.randomUUID(),
                        "IOI_CREATED",
                        "IOI",
                        aggregateId,
                        fixedTime,
                        "correlation-123",
                        1,
                        Map.of(
                                "instrumentId",
                                "BOND-1001"
                        )
                );

        when(outboxEventRepository.save(
                org.mockito.ArgumentMatchers
                        .any(OutboxEvent.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        outboxService.saveEvent(
                envelope,
                "ioi-events",
                aggregateId.toString()
        );


        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(
                        OutboxEvent.class
                );

        verify(outboxEventRepository)
                .save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();

        assertEquals(
                envelope.eventId(),
                savedEvent.getId()
        );

        assertEquals(
                OutboxStatus.PENDING,
                savedEvent.getStatus()
        );

        assertEquals(
                "IOI_CREATED",
                savedEvent.getEventType()
        );

        assertEquals(
                "ioi-events",
                savedEvent.getTopic()
        );

        assertEquals(
                aggregateId.toString(),
                savedEvent.getEventKey()
        );

        assertEquals(
                fixedTime,
                savedEvent.getCreatedAt()
        );

        assertEquals(
                0,
                savedEvent.getRetryCount()
        );

        assertNotNull(savedEvent.getPayload());
    }
}