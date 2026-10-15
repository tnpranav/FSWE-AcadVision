package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fee_ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeLedgerEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_id", length = 60)
  private String invoiceId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "entry_type", nullable = false, length = 10)
  private String entryType; // DEBIT, CREDIT

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false)
  private BigDecimal balanceAfter;

  @Column(nullable = false)
  private String description;

  @Column(name = "transaction_date", insertable = false, updatable = false)
  private OffsetDateTime transactionDate;
}
