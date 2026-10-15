package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "class_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "section_code", length = 60)
  private String sectionCode;

  @Column(name = "session_date", nullable = false)
  private LocalDate sessionDate;

  @Column(name = "session_time")
  private LocalTime sessionTime;

  @Column(name = "topic_covered", length = 200)
  private String topicCovered;

  @Column(name = "faculty_id", nullable = false, length = 60)
  private String facultyId;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
