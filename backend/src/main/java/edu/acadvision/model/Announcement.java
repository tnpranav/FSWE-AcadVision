package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(name = "target_audience", nullable = false, length = 30)
  @Builder.Default
  private String targetAudience = "ALL"; // ALL, STUDENTS, FACULTY, DEPARTMENT

  @Column(name = "department_code", length = 20)
  private String departmentCode;

  @Column(name = "author_id", nullable = false, length = 60)
  private String authorId;

  @Column(nullable = false, length = 15)
  @Builder.Default
  private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

  @Column(name = "is_pinned", nullable = false)
  @Builder.Default
  private Boolean isPinned = false;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
