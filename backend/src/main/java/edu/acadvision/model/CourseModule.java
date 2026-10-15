package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "module_number", nullable = false)
  private Integer moduleNumber;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "hours_allocated", nullable = false)
  @Builder.Default
  private Integer hoursAllocated = 8;
}
