package com.bondtradex.ioi.outbox.service;

public class OutboxSerializationException
        extends RuntimeException {

    public OutboxSerializationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}