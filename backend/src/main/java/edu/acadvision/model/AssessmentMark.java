package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assessment_marks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentMark {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "examination_id", nullable = false)
  private Long examinationId;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "marks_obtained", nullable = false)
  private BigDecimal marksObtained;

  @Column(name = "is_absent", nullable = false)
  @Builder.Default
  private Boolean isAbsent = false;

  @Column(length = 200)
  private String remarks;

  @Column(name = "entered_by", nullable = false, length = 60)
  private String enteredBy;

  @Column(name = "entered_at", insertable = false, updatable = false)
  private OffsetDateTime enteredAt;
}
