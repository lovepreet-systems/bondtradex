CREATE TABLE processed_kafka_events
(
    id BIGSERIAL PRIMARY KEY,

    event_id UUID NOT NULL,

    consumer_name VARCHAR(100) NOT NULL,

    source_topic VARCHAR(255),

    source_partition INTEGER,

    source_offset BIGINT,

    processed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_processed_event
        UNIQUE (event_id, consumer_name)
);

CREATE INDEX idx_processed_event_id
    ON processed_kafka_events(event_id);

CREATE INDEX idx_processed_processed_at
    ON processed_kafka_events(processed_at);



CREATE TABLE dead_letter_events
(
    id BIGSERIAL PRIMARY KEY,

    dead_letter_event_id UUID NOT NULL,

    original_event_id UUID NOT NULL,

    original_topic VARCHAR(255),

    original_partition INTEGER,

    consumer_group VARCHAR(255),

    original_offset BIGINT,

    original_key VARCHAR(255),

    correlation_id VARCHAR(255),

    trace_id VARCHAR(255),

    error_category VARCHAR(100),

    failure_reason TEXT,

    exception_type VARCHAR(255),

    attempt_count INTEGER,

    status VARCHAR(50),

    replay_count INTEGER,

    original_payload JSONB,

    failed_at TIMESTAMPTZ,

    last_replayed_at TIMESTAMPTZ,

    CONSTRAINT uk_dead_letter_event
        UNIQUE (dead_letter_event_id)
);

CREATE INDEX idx_dead_letter_original_event
    ON dead_letter_events(original_event_id);

CREATE INDEX idx_dead_letter_status
    ON dead_letter_events(status);

CREATE INDEX idx_dead_letter_failed_at
    ON dead_letter_events(failed_at);