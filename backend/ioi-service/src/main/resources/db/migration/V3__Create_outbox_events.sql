CREATE TABLE outbox_events
(
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(100) NOT NULL,

    aggregate_id UUID NOT NULL,

    event_type VARCHAR(150) NOT NULL,

    topic VARCHAR(200) NOT NULL,

    event_key VARCHAR(200) NOT NULL,

    payload JSONB NOT NULL,

    status VARCHAR(30) NOT NULL,

    retry_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    published_at TIMESTAMP WITH TIME ZONE,

    next_retry_at TIMESTAMP WITH TIME ZONE,

    last_error VARCHAR(4000),

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_outbox_status
        CHECK (
            status IN (
                       'PENDING',
                       'PROCESSING',
                       'PUBLISHED',
                       'FAILED'
                )
            ),

    CONSTRAINT chk_outbox_retry_count
        CHECK (retry_count >= 0)
);

CREATE INDEX idx_outbox_publishable
    ON outbox_events (
                      status,
                      next_retry_at,
                      created_at
        );

CREATE INDEX idx_outbox_aggregate
    ON outbox_events (
                      aggregate_type,
                      aggregate_id
        );

CREATE INDEX idx_outbox_event_type
    ON outbox_events (
                      event_type
        );

CREATE INDEX idx_outbox_updated_at
    ON outbox_events (
                      updated_at
        );