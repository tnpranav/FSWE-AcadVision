package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "gpa_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpaRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(name = "semester_number", nullable = false)
  private Integer semesterNumber;

  @Column(name = "semester_gpa", nullable = false)
  private BigDecimal semesterGpa;

  @Column(name = "cumulative_cgpa", nullable = false)
  private BigDecimal cumulativeCgpa;

  @Column(name = "credits_attempted", nullable = false)
  private Integer creditsAttempted;

  @Column(name = "credits_earned", nullable = false)
  private Integer creditsEarned;

  @Column(name = "recorded_at", insertable = false, updatable = false)
  private OffsetDateTime recordedAt;
}
