ALTER TABLE payment_receipt_email_outbox
  ADD COLUMN IF NOT EXISTS recipient_email VARCHAR(254);
