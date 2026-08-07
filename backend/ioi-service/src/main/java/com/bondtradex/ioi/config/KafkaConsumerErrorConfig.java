package com.bondtradex.ioi.config;

import com.bondtradex.ioi.downstream.recovery.CustomDeadLetterRecoverer;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler(
            CustomDeadLetterRecoverer recoverer,
            @Value("${app.kafka.consumer.retry.max-attempts}")
            int maximumAttempts,
            @Value("${app.kafka.consumer.retry.initial-interval}")
            Duration initialInterval,
            @Value("${app.kafka.consumer.retry.multiplier}")
            double multiplier,
            @Value("${app.kafka.consumer.retry.max-interval}")
            Duration maximumInterval
    ) {
        if (maximumAttempts < 1) {
            throw new IllegalArgumentException(
                    "Kafka maximum attempts must be at least 1"
            );
        }

        /*
         * maximumAttempts includes the original listener call.
         *
         * Example:
         * maximumAttempts = 4
         * retries = 3
         */
        int maximumRetries = maximumAttempts - 1;

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(
                        maximumRetries
                );

        backOff.setInitialInterval(initialInterval.toMillis());

        backOff.setMultiplier(multiplier);

        backOff.setMaxInterval(maximumInterval.toMillis());

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        backOff
                );

        /*
         * These failures will not improve simply by waiting.
         * They go directly to CustomDeadLetterRecoverer.
         */
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                DeserializationException.class,
                AccessDeniedException.class
        );

        errorHandler.setResetStateOnRecoveryFailure(true);

        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) ->
                        log.warn(
                                "Kafka processing attempt failed. "
                                        + "topic={}, partition={}, "
                                        + "offset={}, deliveryAttempt={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt,
                                exception
                        )
        );

        return errorHandler;
    }
}