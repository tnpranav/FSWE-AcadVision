package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "student_grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGrade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(name = "total_score", nullable = false)
  private BigDecimal totalScore;

  @Column(name = "letter_grade", nullable = false, length = 5)
  private String letterGrade;

  @Column(name = "grade_point", nullable = false)
  private BigDecimal gradePoint;

  @Column(name = "credits_earned", nullable = false)
  private Integer creditsEarned;

  @Column(nullable = false)
  @Builder.Default
  private Boolean published = true;

  @Column(name = "finalized_at", insertable = false, updatable = false)
  private OffsetDateTime finalizedAt;
}
