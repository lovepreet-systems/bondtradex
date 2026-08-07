package com.bondtradex.ioi.outbox.exception;

public class OutboxPublicationException extends RuntimeException {

    public OutboxPublicationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
