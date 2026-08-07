ALTER TABLE outbox_events
DROP CONSTRAINT IF EXISTS outbox_events_status_check;

ALTER TABLE outbox_events
DROP CONSTRAINT IF EXISTS chk_outbox_events_status;

ALTER TABLE outbox_events
    ADD CONSTRAINT chk_outbox_events_status
        CHECK (
            status IN (
                       'PENDING',
                       'PROCESSING',
                       'PUBLISHED',
                       'FAILED',
                       'DEAD'
                )
            );