package com.bondtradex.ioi.outbox.service;

import com.bondtradex.ioi.outbox.entity.OutboxEvent;
import com.bondtradex.ioi.outbox.metrics.OutboxMetrics;
import com.bondtradex.ioi.outbox.status.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OutboxPublisher.class);
    private static final long KAFKA_SEND_TIMEOUT_SECONDS = 10;
    private final OutboxProcessingService processingService;
    private final ResilientKafkaPublisher resilientKafkaPublisher;;
    private final ObjectMapper objectMapper;
    private final OutboxMetrics outboxMetrics;

    public OutboxPublisher(
            OutboxProcessingService processingService,
            KafkaTemplate<String, Object> kafkaTemplate, ResilientKafkaPublisher resilientKafkaPublisher,
            ObjectMapper objectMapper,
            OutboxMetrics outboxMetrics
    ) {
        this.processingService = processingService;
        this.resilientKafkaPublisher = resilientKafkaPublisher;
        this.objectMapper = objectMapper;
        this.outboxMetrics=outboxMetrics;
    }

    @Scheduled(
            fixedDelayString = "${app.outbox.publisher.fixed-delay-ms}"
    )
    public void publishPendingEvents() {
        List<OutboxEvent> events;

        try {
            events = processingService.claimPublishableEvents();
            outboxMetrics.recordBatchSize(events.size());
        } catch (Exception exception) {
            LOGGER.error(
                    "Failed to claim publishable outbox events",
                    exception
            );
            return;
        }

        for (OutboxEvent event : events) {
            publish(event);
        }
    }
    private void publish(OutboxEvent event) {

        long startTime = System.nanoTime();

        try {

            JsonNode message =
                    deserializePayload(event.getPayload());

            ProducerRecord<String, Object> producerRecord =
                    new ProducerRecord<>(
                            event.getTopic(),
                            event.getEventKey(),
                            message
                    );

            resilientKafkaPublisher
                    .publish(producerRecord)
                    .get(
                            KAFKA_SEND_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            processingService.markPublished(event.getId());

            outboxMetrics.recordPublished();

            LOGGER.info(
                    "Published outbox event. eventId={}, eventType={}, topic={}",
                    event.getId(),
                    event.getEventType(),
                    event.getTopic()
            );

        } catch (Exception exception) {

            handleFailure(event, exception);

        } finally {

            outboxMetrics.recordPublishingDuration(
                    System.nanoTime() - startTime
            );
        }
    }

    private JsonNode deserializePayload(
            String payload
    ) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            throw new OutboxSerializationException(
                    "Failed to deserialize outbox event payload",
                    exception
            );
        }
    }
    private void handleFailure(
            OutboxEvent event,
            Exception exception
    ) {
        String errorMessage = getErrorMessage(exception);

        try {
            OutboxStatus resultingStatus =
                    processingService.markFailed(
                            event.getId(),
                            errorMessage
                    );
            outboxMetrics.recordFailure(resultingStatus);
        } catch (Exception statusUpdateException) {
            LOGGER.error(
                    "Failed to update outbox event status. eventId={}",
                    event.getId(),
                    statusUpdateException
            );
        }

        LOGGER.error(
                "Failed to publish outbox event. "
                        + "eventId={}, eventType={}, topic={}",
                event.getId(),
                event.getEventType(),
                event.getTopic(),
                exception
        );
    }

    private String getErrorMessage(Exception exception) {
        Throwable cause = exception.getCause();

        if (cause != null
                && cause.getMessage() != null
                && !cause.getMessage().isBlank()) {
            return cause.getClass().getSimpleName()
                    + ": "
                    + cause.getMessage();
        }

        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName()
                    + ": "
                    + exception.getMessage();
        }

        return exception.getClass().getName();
    }
}
