package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_reference", nullable = false, unique = true, length = 80)
  private String transactionReference;

  @Column(name = "invoice_id", nullable = false, length = 60)
  private String invoiceId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "payment_method", nullable = false, length = 30)
  private String paymentMethod; // UPI, NET_BANKING, CARD, SCHOLARSHIP_ADJUSTMENT

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "SUCCESS"; // INITIATED, SUCCESS, FAILED, REFUNDED

  @Column(name = "provider_reference", length = 160)
  private String providerReference;

  @Column(name = "receipt_number", unique = true, length = 80)
  private String receiptNumber;

  @Column(name = "verified_at")
  private OffsetDateTime verifiedAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
