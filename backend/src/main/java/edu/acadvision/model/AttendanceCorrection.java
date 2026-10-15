package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance_corrections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCorrection {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "attendance_record_id", nullable = false)
  private Long attendanceRecordId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "requested_status", nullable = false, length = 20)
  private String requestedStatus; // PRESENT, EXCUSED

  @Column(columnDefinition = "TEXT", nullable = false)
  private String reason;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "PENDING"; // PENDING, APPROVED, REJECTED

  @Column(name = "reviewed_by", length = 60)
  private String reviewedBy;

  @Column(name = "reviewed_at")
  private OffsetDateTime reviewedAt;

  @Column(name = "reviewer_remarks", columnDefinition = "TEXT")
  private String reviewerRemarks;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
