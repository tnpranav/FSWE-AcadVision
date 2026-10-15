package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "student_module_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentModuleProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "module_id", nullable = false)
  private Long moduleId;

  @Column(name = "is_completed", nullable = false)
  @Builder.Default
  private Boolean isCompleted = false;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;
}
