package com.bondtradex.ioi.downstream.recovery;

import com.bondtradex.ioi.downstream.model.DeadLetterEventMessage;
import com.bondtradex.ioi.downstream.model.ErrorCategory;
import com.bondtradex.ioi.downstream.service.KafkaFailureClassifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomDeadLetterRecoverer
        implements ConsumerRecordRecoverer {

    private static final int MAX_FAILURE_REASON_LENGTH = 2_000;

    private static final long DLT_SEND_TIMEOUT_SECONDS = 10L;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final KafkaFailureClassifier failureClassifier;

    private final Clock clock;

    private final String deadLetterTopic;

    private final String consumerGroup;

    private final int maximumAttempts;

    public CustomDeadLetterRecoverer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            KafkaFailureClassifier failureClassifier,
            Clock clock,
            @Value("${app.kafka.topics.ioi-events-dlt}")
            String deadLetterTopic,
            @Value("${spring.kafka.consumer.group-id}")
            String consumerGroup,
            @Value("${app.kafka.consumer.retry.max-attempts}")
            int maximumAttempts
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.failureClassifier = failureClassifier;
        this.clock = clock;
        this.deadLetterTopic = deadLetterTopic;
        this.consumerGroup = consumerGroup;
        this.maximumAttempts = maximumAttempts;
    }

    @Override
    public void accept(ConsumerRecord<?, ?> record, Exception exception) {
        Throwable rootCause =
                failureClassifier.findRootCause(exception);

        ErrorCategory errorCategory =
                failureClassifier.classify(rootCause);

        JsonNode originalPayload =
                convertPayload(record.value());

        UUID originalEventId =
                extractOriginalEventId(
                        originalPayload,
                        record
                );

        String correlationId =
                extractCorrelationId(
                        originalPayload,
                        record
                );

        String traceId =
                extractTraceId(
                        originalPayload,
                        record
                );

        int attemptCount =
                determineAttemptCount(rootCause);

        DeadLetterEventMessage deadLetterMessage =
                new DeadLetterEventMessage(
                        UUID.randomUUID(),
                        originalEventId,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key() == null
                                ? null
                                : record.key().toString(),
                        originalPayload,
                        correlationId,
                        traceId,
                        consumerGroup,
                        errorCategory,
                        boundedFailureReason(rootCause),
                        rootCause.getClass().getName(),
                        attemptCount,
                        Instant.now(clock),
                        "ioi-service"
                );

        ProducerRecord<String, Object> producerRecord =
                new ProducerRecord<>(
                        deadLetterTopic,
                        record.partition(),
                        record.key() == null
                                ? null
                                : record.key().toString(),
                        deadLetterMessage
                );

        try {
            kafkaTemplate.send(producerRecord)
                    .get(
                            DLT_SEND_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            log.error(
                    "Kafka record moved to DLT. "
                            + "deadLetterEventId={}, "
                            + "originalEventId={}, "
                            + "originalTopic={}, "
                            + "originalPartition={}, "
                            + "originalOffset={}, "
                            + "errorCategory={}, "
                            + "attemptCount={}",
                    deadLetterMessage.deadLetterEventId(),
                    deadLetterMessage.originalEventId(),
                    deadLetterMessage.originalTopic(),
                    deadLetterMessage.originalPartition(),
                    deadLetterMessage.originalOffset(),
                    deadLetterMessage.errorCategory(),
                    deadLetterMessage.attemptCount(),
                    rootCause
            );

        } catch (Exception dltPublishingException) {
            /*
             * Do not silently recover the original record when the DLT
             * publication itself has failed. Throwing here tells the
             * error handler that recovery was unsuccessful.
             */
            throw new KafkaException(
                    "Failed to publish structured message to DLT. "
                            + "originalTopic=" + record.topic()
                            + ", originalPartition=" + record.partition()
                            + ", originalOffset=" + record.offset(),
                    dltPublishingException
            );
        }
    }

    private JsonNode convertPayload(Object value) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        if (value instanceof JsonNode jsonNode) {
            return jsonNode;
        }

        if (value instanceof String stringValue) {
            try {
                return objectMapper.readTree(stringValue);
            } catch (Exception ignored) {
                /*
                 * The value may be a normal string rather than JSON.
                 * Preserve it as a JSON string instead of losing it.
                 */
                return objectMapper.valueToTree(stringValue);
            }
        }

        return objectMapper.valueToTree(value);
    }

    private UUID extractOriginalEventId(
            JsonNode payload,
            ConsumerRecord<?, ?> record
    ) {
        UUID payloadEventId =
                extractUuid(payload, "eventId");

        if (payloadEventId != null) {
            return payloadEventId;
        }

        String eventIdHeader =
                readHeader(record, "eventId");

        if (eventIdHeader != null) {
            try {
                return UUID.fromString(eventIdHeader);
            } catch (IllegalArgumentException ignored) {
                log.warn(
                        "Invalid eventId Kafka header. value={}",
                        eventIdHeader
                );
            }
        }

        /*
         * A poison record must still be recoverable even when it lacks
         * a valid eventId. Generate a stable identifier from its Kafka
         * coordinates so repeated recovery of the same record produces
         * the same fallback ID.
         */
        String coordinates =
                record.topic()
                        + ":"
                        + record.partition()
                        + ":"
                        + record.offset();

        log.warn(
                "Original eventId missing. "
                        + "Using deterministic Kafka-coordinate ID. "
                        + "coordinates={}",
                coordinates
        );

        return UUID.nameUUIDFromBytes(
                coordinates.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String extractCorrelationId(
            JsonNode payload,
            ConsumerRecord<?, ?> record
    ) {
        String payloadValue =
                extractText(payload, "correlationId");

        if (payloadValue != null) {
            return payloadValue;
        }

        String headerValue =
                readHeader(record, "correlationId");

        if (headerValue != null) {
            return headerValue;
        }

        return readHeader(record, "X-Correlation-Id");
    }

    private String extractTraceId(
            JsonNode payload,
            ConsumerRecord<?, ?> record
    ) {
        String payloadValue =
                extractText(payload, "traceId");

        if (payloadValue != null) {
            return payloadValue;
        }

        String traceIdHeader =
                readHeader(record, "traceId");

        if (traceIdHeader != null) {
            return traceIdHeader;
        }

        /*
         * W3C traceparent:
         * version-traceId-spanId-flags
         */
        String traceParent =
                readHeader(record, "traceparent");

        if (traceParent == null) {
            return null;
        }

        String[] parts = traceParent.split("-");

        if (parts.length >= 4) {
            return parts[1];
        }

        return null;
    }

    private UUID extractUuid(
            JsonNode payload,
            String fieldName
    ) {
        String value =
                extractText(payload, fieldName);

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Invalid UUID in event payload. "
                            + "field={}, value={}",
                    fieldName,
                    value
            );

            return null;
        }
    }

    private String extractText(
            JsonNode payload,
            String fieldName
    ) {
        if (payload == null
                || !payload.hasNonNull(fieldName)) {
            return null;
        }

        String value =
                payload.get(fieldName).asText();

        return value == null || value.isBlank()
                ? null
                : value;
    }

    private String readHeader(
            ConsumerRecord<?, ?> record,
            String headerName
    ) {
        Header header =
                record.headers().lastHeader(headerName);

        if (header == null || header.value() == null) {
            return null;
        }

        return new String(
                header.value(),
                StandardCharsets.UTF_8
        );
    }

    private int determineAttemptCount(
            Throwable rootCause
    ) {
        /*
         * Retryable failures reach the recoverer only after all configured
         * attempts have been used. Non-retryable failures go directly to
         * recovery after the first listener attempt.
         */
        return failureClassifier.isRetryable(rootCause) ? maximumAttempts : 1;
    }

    private String boundedFailureReason(
            Throwable rootCause
    ) {
        String message = rootCause.getMessage();

        if (message == null || message.isBlank()) {
            return rootCause.getClass().getSimpleName();
        }

        if (message.length() <= MAX_FAILURE_REASON_LENGTH) {
            return message;
        }

        return message.substring(
                0,
                MAX_FAILURE_REASON_LENGTH
        );
    }
}