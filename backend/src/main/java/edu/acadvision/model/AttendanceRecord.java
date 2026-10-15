package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false, length = 60)
  private String studentId;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "session_id")
  private Long sessionId;

  @Column(name = "class_date", nullable = false)
  private LocalDate classDate;

  @Column(nullable = false, length = 20)
  private String status; // PRESENT, ABSENT, LATE, EXCUSED

  @Column(length = 200)
  private String remarks;

  @Column(name = "marked_by", nullable = false, length = 60)
  private String markedBy;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private OffsetDateTime updatedAt;
}
