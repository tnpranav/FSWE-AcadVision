CREATE TABLE IF NOT EXISTS payment_receipt_email_outbox (
  id BIGSERIAL PRIMARY KEY,
  razorpay_payment_order_id BIGINT NOT NULL UNIQUE
    REFERENCES razorpay_payment_orders(id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED')),
  attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
  next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  locked_at TIMESTAMPTZ,
  sent_at TIMESTAMPTZ,
  last_error VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_receipt_email_outbox_due
  ON payment_receipt_email_outbox(status, next_attempt_at, id);
