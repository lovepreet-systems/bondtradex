package com.bondtradex.ioi.downstream.service;

import com.bondtradex.ioi.downstream.model.ErrorCategory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class KafkaFailureClassifier {

    public ErrorCategory classify(Throwable throwable) {
        Throwable rootCause = findRootCause(throwable);

        if (rootCause instanceof DeserializationException) {
            return ErrorCategory.DESERIALIZATION_ERROR;
        }

        if (rootCause instanceof AccessDeniedException) {
            return ErrorCategory.AUTHORIZATION_ERROR;
        }

        if (rootCause instanceof IllegalArgumentException) {
            return ErrorCategory.VALIDATION_ERROR;
        }

        if (rootCause instanceof DataAccessException) {
            return ErrorCategory.TRANSIENT_INFRASTRUCTURE_ERROR;
        }

        return ErrorCategory.UNKNOWN_ERROR;
    }

    public boolean isRetryable(Throwable throwable) {
        ErrorCategory category = classify(throwable);

        return switch (category) {
            case TRANSIENT_INFRASTRUCTURE_ERROR,
                 DEPENDENCY_ERROR -> true;

            case VALIDATION_ERROR,
                 DESERIALIZATION_ERROR,
                 AUTHORIZATION_ERROR,
                 UNKNOWN_ERROR -> false;
        };
    }

    public Throwable findRootCause(Throwable throwable) {
        if (throwable == null) {
            return new IllegalStateException(
                    "Kafka processing failed without an exception"
            );
        }

        Throwable current = throwable;

        /*
         * Kafka listener exceptions are commonly wrapped inside
         * ListenerExecutionFailedException and other framework exceptions.
         */
        while (current.getCause() != null
                && current.getCause() != current) {
            current = current.getCause();
        }

        return current;
    }
}