package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "section_code", length = 60)
  private String sectionCode;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "ENROLLED"; // ENROLLED, DROPPED, COMPLETED, FAILED

  @Column(name = "enrollment_date", nullable = false)
  @Builder.Default
  private LocalDate enrollmentDate = LocalDate.now();

  @Column(name = "drop_date")
  private LocalDate dropDate;
}
