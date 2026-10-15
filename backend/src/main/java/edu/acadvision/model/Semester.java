package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "academic_year_id", nullable = false)
  private Long academicYearId;

  @Column(name = "semester_number", nullable = false)
  private Integer semesterNumber;

  @Column(nullable = false, length = 20)
  private String term; // FALL, SPRING, SUMMER

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "is_current", nullable = false)
  @Builder.Default
  private Boolean isCurrent = false;

  @Column(name = "registration_open", nullable = false)
  @Builder.Default
  private Boolean registrationOpen = false;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
