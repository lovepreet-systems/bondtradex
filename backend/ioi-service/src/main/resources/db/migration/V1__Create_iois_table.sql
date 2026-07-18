CREATE TABLE iois (
    id UUID PRIMARY KEY,
    ioi_number VARCHAR(50) NOT NULL UNIQUE,

    client_id UUID NOT NULL,
    instrument_id UUID NOT NULL,

    isin VARCHAR(12),
    cusip VARCHAR(9),

    side VARCHAR(10) NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    target_price NUMERIC(19, 4),
    currency VARCHAR(3) NOT NULL,
    settlement_date DATE,

    status VARCHAR(40) NOT NULL,

    client_comment VARCHAR(1000),
    sales_comment VARCHAR(1000),
    trader_comment VARCHAR(1000),
    rejection_reason VARCHAR(1000),

    sales_user_id UUID,
    trader_user_id UUID,

    submitted_at TIMESTAMPTZ,
    sales_reviewed_at TIMESTAMPTZ,
    trader_reviewed_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(100) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_iois_client_id
    ON iois(client_id);

CREATE INDEX idx_iois_instrument_id
    ON iois(instrument_id);

CREATE INDEX idx_iois_status
    ON iois(status);

CREATE INDEX idx_iois_created_at
    ON iois(created_at);

CREATE INDEX idx_iois_status_created_at
    ON iois(status, created_at);