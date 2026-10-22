package edu.acadvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PaymentReceiptEmailOutbox {
  private static final Logger log = LoggerFactory.getLogger(PaymentReceiptEmailOutbox.class);
  private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
  private static final DateTimeFormatter PAID_AT_FORMAT =
      DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a 'IST'", Locale.ENGLISH).withZone(INDIA_ZONE);

  private final JdbcTemplate jdbc;
  // private final JavaMailSender mailSender;
  private final ResendEmailClient resendEmailClient;
  private final TransactionTemplate transactions;
  private final String mailbox;
  private final boolean mailConfigured;

  public PaymentReceiptEmailOutbox(JdbcTemplate jdbc, ResendEmailClient resendEmailClient, 
      PlatformTransactionManager transactionManager,
      @Value("${spring.mail.username:}") String mailbox,
      @Value("${spring.mail.password:}") String mailPassword) {
    this.jdbc = jdbc;
    this.resendEmailClient = resendEmailClient;
    // this.mailSender = mailSender;
    this.transactions = new TransactionTemplate(transactionManager);
    this.mailbox = mailbox == null ? "" : mailbox.trim();
    this.mailConfigured = !this.mailbox.isBlank() && mailPassword != null && !mailPassword.isBlank();
  }

  @Scheduled(fixedDelayString = "${app.payment-receipt-email.poll-interval-ms:5000}")
  public void dispatchPendingReceipts() {
    if (!mailConfigured) return;
    for (Long outboxId : claimDueBatch()) sendReceipt(outboxId);
  }

  private List<Long> claimDueBatch() {
    return transactions.execute(status -> jdbc.query("""
        WITH due AS (
          SELECT id
          FROM payment_receipt_email_outbox
          WHERE ((status IN ('PENDING', 'FAILED') AND next_attempt_at <= CURRENT_TIMESTAMP)
             OR (status = 'SENDING' AND locked_at < CURRENT_TIMESTAMP - INTERVAL '5 minutes'))
          ORDER BY id
          LIMIT 10
          FOR UPDATE SKIP LOCKED
        )
        UPDATE payment_receipt_email_outbox AS outbox
        SET status = 'SENDING', attempts = outbox.attempts + 1,
            locked_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
        FROM due
        WHERE outbox.id = due.id
        RETURNING outbox.id
        """, (rs, rowNum) -> rs.getLong(1)));
  }

  private void sendReceipt(long outboxId) {
    try {
      Receipt receipt = jdbc.queryForObject("""
          SELECT COALESCE(e.recipient_email, ?) AS recipient_email,
                 s.full_name AS student_name, o.invoice_id, f.description,
                 p.amount, o.currency, p.payment_method, o.razorpay_payment_id,
                 o.receipt_number, COALESCE(o.verified_at, p.verified_at, CURRENT_TIMESTAMP) AS paid_at
          FROM payment_receipt_email_outbox e
          JOIN razorpay_payment_orders o ON o.id = e.razorpay_payment_order_id
          JOIN students s ON s.student_id = o.student_id
          JOIN fee_invoices f ON f.invoice_id = o.invoice_id
          JOIN payment_transactions p
            ON p.invoice_id = o.invoice_id AND p.provider_reference = o.razorpay_payment_id
          WHERE e.id = ? AND e.status = 'SENDING'
          """, (rs, rowNum) -> mapReceipt(rs), mailbox, outboxId);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(mailbox);
      message.setTo(receipt.recipientEmail());
      message.setSubject("AcadVision payment receipt " + receipt.receiptNumber());
      message.setText(formatMessage(receipt));
      // mailSender.send(message);
      resendEmailClient.send(message);

      jdbc.update("""
          UPDATE payment_receipt_email_outbox
          SET status = 'SENT', sent_at = CURRENT_TIMESTAMP, locked_at = NULL,
              last_error = NULL, updated_at = CURRENT_TIMESTAMP
          WHERE id = ? AND status = 'SENDING'
          """, outboxId);
      log.info("Payment receipt email sent for outbox {}", outboxId);
    } catch (Exception ex) {
      recordFailure(outboxId, ex);
    }
  }

  private Receipt mapReceipt(ResultSet rs) throws SQLException {
    Instant paidAt = rs.getTimestamp("paid_at").toInstant();
    return new Receipt(rs.getString("recipient_email"), rs.getString("student_name"), rs.getString("invoice_id"),
        rs.getString("description"), rs.getBigDecimal("amount"), rs.getString("currency"),
        rs.getString("payment_method"), rs.getString("razorpay_payment_id"),
        rs.getString("receipt_number"), paidAt);
  }

  private String formatMessage(Receipt receipt) {
    return "Hello " + receipt.studentName() + ",\n\n"
        + "Your AcadVision fee payment has been verified and recorded.\n\n"
        + "Invoice: " + receipt.invoiceId() + "\n"
        + "Description: " + receipt.description() + "\n"
        + "Amount: " + receipt.currency() + " " + receipt.amount().setScale(2).toPlainString() + "\n"
        + "Status: PAID\n"
        + "Paid at: " + PAID_AT_FORMAT.format(receipt.paidAt()) + "\n"
        + "Payment method: " + receipt.paymentMethod() + "\n"
        + "Razorpay payment reference: " + receipt.paymentId() + "\n"
        + "AcadVision receipt number: " + receipt.receiptNumber() + "\n\n"
        + "This receipt was sent to the email address provided in the AcadVision student portal.\n";
  }

  private void recordFailure(long outboxId, Exception ex) {
    try {
      Integer attempts = jdbc.queryForObject(
          "SELECT attempts FROM payment_receipt_email_outbox WHERE id = ?", Integer.class, outboxId);
      int currentAttempt = attempts == null ? 1 : attempts;
      int retryMinutes = Math.min(60, 1 << Math.min(currentAttempt, 6));
      String errorCode = ex.getClass().getSimpleName();
      jdbc.update("""
          UPDATE payment_receipt_email_outbox
          SET status = 'FAILED', next_attempt_at = CURRENT_TIMESTAMP + (? * INTERVAL '1 minute'),
              locked_at = NULL, last_error = ?, updated_at = CURRENT_TIMESTAMP
          WHERE id = ?
          """, retryMinutes, errorCode, outboxId);
      log.warn("Payment receipt email delivery failed for outbox {} ({})", outboxId, errorCode);
    } catch (Exception updateError) {
      log.error("Could not record payment receipt email failure for outbox {}", outboxId,
          updateError.getClass().getSimpleName());
    }
  }

  private record Receipt(String recipientEmail, String studentName, String invoiceId, String description, BigDecimal amount,
      String currency, String paymentMethod, String paymentId, String receiptNumber, Instant paidAt) {}
}
