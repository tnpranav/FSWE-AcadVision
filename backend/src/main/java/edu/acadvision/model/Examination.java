package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "examinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Examination {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(name = "exam_type", nullable = false, length = 30)
  private String examType; // MID_TERM, END_SEMESTER, QUIZ, PRACTICAL

  @Column(name = "exam_date", nullable = false)
  private LocalDate examDate;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "max_marks", nullable = false)
  @Builder.Default
  private BigDecimal maxMarks = BigDecimal.valueOf(100.0);

  @Column(nullable = false)
  @Builder.Default
  private BigDecimal weightage = BigDecimal.valueOf(30.0);

  @Column(name = "classroom_id")
  private Long classroomId;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
