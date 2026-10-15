package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assignment_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "assignment_id", nullable = false)
  private Long assignmentId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "submission_date", nullable = false)
  @Builder.Default
  private OffsetDateTime submissionDate = OffsetDateTime.now();

  @Column(name = "file_url", length = 255)
  private String fileUrl;

  @Column(columnDefinition = "TEXT")
  private String content;

  private BigDecimal score;

  @Column(columnDefinition = "TEXT")
  private String feedback;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "SUBMITTED"; // SUBMITTED, LATE, GRADED, RETURNED

  @Column(name = "graded_by", length = 60)
  private String gradedBy;

  @Column(name = "graded_at")
  private OffsetDateTime gradedAt;
}
