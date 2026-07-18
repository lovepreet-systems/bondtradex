ALTER TABLE iois
    ADD COLUMN offering_id UUID;

ALTER TABLE iois
    ADD COLUMN offering_created_at TIMESTAMPTZ;

CREATE UNIQUE INDEX uk_iois_offering_id
    ON iois(offering_id)
    WHERE offering_id IS NOT NULL;