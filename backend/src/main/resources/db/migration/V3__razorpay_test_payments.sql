CREATE TABLE IF NOT EXISTS razorpay_payment_orders (
  id BIGSERIAL PRIMARY KEY,
  razorpay_order_id VARCHAR(80) NOT NULL UNIQUE,
  invoice_id VARCHAR(60) NOT NULL REFERENCES fee_invoices(invoice_id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  status VARCHAR(20) NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'PAID', 'FAILED')),
  razorpay_payment_id VARCHAR(80) UNIQUE,
  receipt_number VARCHAR(80) UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  verified_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_razorpay_orders_student_invoice
  ON razorpay_payment_orders(student_id, invoice_id, created_at DESC);

ALTER TABLE payment_transactions
  DROP CONSTRAINT IF EXISTS payment_transactions_payment_method_check;
ALTER TABLE payment_transactions
  ADD CONSTRAINT payment_transactions_payment_method_check
  CHECK (payment_method IN ('UPI', 'NET_BANKING', 'CARD', 'WALLET', 'EMI', 'PAYLATER', 'BANK_TRANSFER', 'CARDLESS_EMI', 'RAZORPAY', 'SCHOLARSHIP_ADJUSTMENT'));
