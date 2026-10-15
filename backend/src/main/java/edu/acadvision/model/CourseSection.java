package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "section_code", nullable = false, unique = true, length = 60)
  private String sectionCode;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "semester_id")
  private Long semesterId;

  @Column(name = "faculty_id", nullable = false, length = 60)
  private String facultyId;

  @Column(name = "classroom_id")
  private Long classroomId;

  @Column(name = "max_capacity", nullable = false)
  @Builder.Default
  private Integer maxCapacity = 60;

  @Column(name = "enrolled_count", nullable = false)
  @Builder.Default
  private Integer enrolledCount = 0;
}
