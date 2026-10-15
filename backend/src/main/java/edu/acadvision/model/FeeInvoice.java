package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fee_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInvoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_id", nullable = false, unique = true, length = 60)
  private String invoiceId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(name = "fee_type", nullable = false, length = 40)
  @Builder.Default
  private String feeType = "TUITION"; // TUITION, EXAMINATION, LIBRARY_LAB, HOSTEL, TRANSPORT, MISC

  @Column(nullable = false, length = 200)
  private String description;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "discount_amount", nullable = false)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "net_payable", nullable = false)
  private BigDecimal netPayable;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false, length = 30)
  @Builder.Default
  private String status = "PENDING"; // PENDING, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
