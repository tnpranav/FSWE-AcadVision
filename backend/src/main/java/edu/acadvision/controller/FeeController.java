package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import edu.acadvision.service.RazorpayGateway;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/fees")
public class FeeController {
  private final JdbcTemplate jdbc;
  private final RazorpayGateway razorpay;

  public FeeController(JdbcTemplate jdbc, RazorpayGateway razorpay) {
    this.jdbc = jdbc;
    this.razorpay = razorpay;
  }

  @GetMapping("/{studentId}")
  public List<Map<String,Object>> byStudent(@PathVariable String studentId,
      @AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    if (!"ADMIN".equals(principal.getRole()) && (!"STUDENT".equals(principal.getRole()) || !principal.getUserId().equals(studentId))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may only view your own fee records");
    }
    return jdbc.queryForList("SELECT invoice_id,student_id,description,amount,due_date,status,created_at FROM fee_invoices WHERE student_id=? ORDER BY due_date DESC",studentId);
  }

  @PostMapping("/{invoiceId}/razorpay-order")
  @Transactional
  public Map<String,Object> createRazorpayOrder(@PathVariable String invoiceId,
      @AuthenticationPrincipal UserPrincipal principal) {
    requireStudent(principal);
    List<Map<String,Object>> rows = jdbc.queryForList(
        "SELECT net_payable,status,description FROM fee_invoices WHERE invoice_id=? AND student_id=? FOR UPDATE",
        invoiceId, principal.getUserId());
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
    Map<String,Object> invoice = rows.getFirst();
    if ("PAID".equals(invoice.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice is already paid");
    BigDecimal amount = (BigDecimal) invoice.get("net_payable");
    long amountPaise;
    try { amountPaise = amount.movePointRight(2).longValueExact(); }
    catch (ArithmeticException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice amount cannot be charged in paise"); }
    if (amountPaise <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice has no payable balance");

    String receipt = "AV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    Map<String,Object> order = razorpay.createOrder(amountPaise, receipt, invoiceId, principal.getUserId());
    String orderId = String.valueOf(order.get("id"));
    if (orderId.isBlank() || "null".equals(orderId)) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Razorpay did not return an order ID");
    jdbc.update("INSERT INTO razorpay_payment_orders(razorpay_order_id,invoice_id,student_id,amount_paise,currency) VALUES(?,?,?,?,'INR')",
        orderId, invoiceId, principal.getUserId(), amountPaise);
    return Map.of("keyId", razorpay.getKeyId(), "orderId", orderId, "amount", amountPaise,
        "currency", "INR", "name", "AcadVision", "description", String.valueOf(rows.getFirst().getOrDefault("description", "Campus fee payment")),
        "prefill", Map.of("name", principal.getFullName(), "email", principal.getEmail()));
  }

  @PostMapping("/razorpay/verify")
  @Transactional
  public Map<String,Object> verifyRazorpayPayment(@Valid @RequestBody VerifyPayment input,
      @AuthenticationPrincipal UserPrincipal principal) {
    requireStudent(principal);
    List<Map<String,Object>> attempts = jdbc.queryForList("""
        SELECT id,invoice_id,student_id,amount_paise,currency,status,razorpay_payment_id,receipt_number
        FROM razorpay_payment_orders WHERE razorpay_order_id=? AND student_id=? FOR UPDATE
        """, input.razorpayOrderId(), principal.getUserId());
    if (attempts.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Razorpay order not found for this student");
    Map<String,Object> attempt = attempts.getFirst();
    if (!razorpay.verifySignature(input.razorpayOrderId(), input.razorpayPaymentId(), input.razorpaySignature())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay payment signature is invalid");
    }
    if ("PAID".equals(attempt.get("status"))) {
      if (input.razorpayPaymentId().equals(attempt.get("razorpay_payment_id"))) {
        return Map.of("status", "SUCCESS", "receiptNumber", attempt.get("receipt_number"), "invoiceId", attempt.get("invoice_id"));
      }
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This order has already been paid");
    }

    long expectedAmount = ((Number) attempt.get("amount_paise")).longValue();
    Map<String,Object> payment = razorpay.fetchPayment(input.razorpayPaymentId());
    validatePayment(payment, input.razorpayOrderId(), expectedAmount);
    if (!Boolean.TRUE.equals(payment.get("captured")) && "authorized".equals(payment.get("status"))) {
      payment = razorpay.capturePayment(input.razorpayPaymentId(), expectedAmount);
      validatePayment(payment, input.razorpayOrderId(), expectedAmount);
    }
    if (!Boolean.TRUE.equals(payment.get("captured")) || !"captured".equals(payment.get("status"))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Razorpay has not captured this payment yet");
    }

    String paymentMethod = normalizePaymentMethod(String.valueOf(payment.getOrDefault("method", "razorpay")));
    String receiptNumber = "AVR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
    String invoiceId = String.valueOf(attempt.get("invoice_id"));
    BigDecimal paidAmount = BigDecimal.valueOf(expectedAmount, 2);
    jdbc.update("""
        INSERT INTO payment_transactions(transaction_reference,invoice_id,student_id,amount,payment_method,status,provider_reference,receipt_number)
        VALUES(?,?,?,?,?,'SUCCESS',?,?)
        """, "RZP-" + input.razorpayPaymentId(), invoiceId, principal.getUserId(), paidAmount,
        paymentMethod, input.razorpayPaymentId(), receiptNumber);
    jdbc.update("UPDATE fee_invoices SET status='PAID' WHERE invoice_id=? AND student_id=?", invoiceId, principal.getUserId());
    jdbc.update("""
        INSERT INTO fee_ledger_entries(invoice_id,student_id,entry_type,amount,balance_after,description)
        VALUES(?,?,'CREDIT',?,0,?)
        """, invoiceId, principal.getUserId(), paidAmount, "Razorpay test-mode payment " + input.razorpayPaymentId());
    jdbc.update("""
        UPDATE razorpay_payment_orders SET status='PAID',razorpay_payment_id=?,receipt_number=?,verified_at=? WHERE id=?
        """, input.razorpayPaymentId(), receiptNumber, OffsetDateTime.now(), attempt.get("id"));
    jdbc.update("""
        INSERT INTO payment_receipt_email_outbox(razorpay_payment_order_id,recipient_email)
        VALUES(?,?) ON CONFLICT (razorpay_payment_order_id) DO NOTHING
        """, attempt.get("id"), input.recipientEmail().trim());
    return Map.of("status", "SUCCESS", "receiptNumber", receiptNumber, "invoiceId", invoiceId);
  }

  private void requireStudent(UserPrincipal principal) {
    if (principal == null || !"STUDENT".equals(principal.getRole())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
  }

  private void validatePayment(Map<String,Object> payment, String orderId, long amountPaise) {
    if (!orderId.equals(payment.get("order_id")) || !(payment.get("amount") instanceof Number amount)
        || amount.longValue() != amountPaise || !"INR".equals(payment.get("currency"))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay payment does not match this invoice");
    }
  }

  private String normalizePaymentMethod(String method) {
    return switch (method.toLowerCase()) {
      case "upi" -> "UPI";
      case "netbanking" -> "NET_BANKING";
      case "card" -> "CARD";
      case "wallet" -> "WALLET";
      case "emi" -> "EMI";
      case "paylater" -> "PAYLATER";
      case "bank_transfer" -> "BANK_TRANSFER";
      case "cardless_emi" -> "CARDLESS_EMI";
      default -> "RAZORPAY";
    };
  }

  public record VerifyPayment(@NotBlank String razorpayOrderId, @NotBlank String razorpayPaymentId,
      @NotBlank String razorpaySignature,
      @NotBlank @Email @Size(max = 254) String recipientEmail) {}
}
