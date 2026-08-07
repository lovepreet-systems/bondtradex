package com.bondtradex.ioi.outbox.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResilientKafkaPublisher {

    private static final String CIRCUIT_NAME = "kafkaPublisher";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @CircuitBreaker(
            name = CIRCUIT_NAME,
            fallbackMethod = "fallback"
    )
    @TimeLimiter(name = CIRCUIT_NAME)
    public CompletableFuture<SendResult<String, Object>> publish(
            ProducerRecord<String, Object> record
    ) {

        return kafkaTemplate.send(record);
    }

    private CompletableFuture<SendResult<String, Object>> fallback(
            ProducerRecord<String, Object> record,
            Throwable throwable
    ) {
        return CompletableFuture.failedFuture(throwable);
    }
}
