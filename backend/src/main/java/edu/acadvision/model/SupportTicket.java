package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ticket_number", nullable = false, unique = true, length = 40)
  private String ticketNumber;

  @Column(name = "user_id", nullable = false, length = 60)
  private String userId;

  @Column(nullable = false, length = 40)
  private String category; // ACADEMIC, ATTENDANCE, FEE_PAYMENT, TIMETABLE, TECHNICAL

  @Column(nullable = false, length = 200)
  private String subject;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Column(nullable = false, length = 15)
  @Builder.Default
  private String priority = "MEDIUM";

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

  @Column(name = "assigned_to", length = 60)
  private String assignedTo;

  @Column(name = "resolution_notes", columnDefinition = "TEXT")
  private String resolutionNotes;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "resolved_at")
  private OffsetDateTime resolvedAt;
}
