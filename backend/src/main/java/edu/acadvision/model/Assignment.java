package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
public class Assignment {
  @Builder
  public Assignment(String courseCode, String sectionCode, String facultyId, String title,
      String description, BigDecimal totalMarks, BigDecimal weightage, OffsetDateTime dueDate,
      String attachmentUrl, Boolean published) {
    this.courseCode = courseCode;
    this.sectionCode = sectionCode;
    this.facultyId = facultyId;
    this.title = title;
    this.description = description;
    this.totalMarks = totalMarks != null ? totalMarks : BigDecimal.valueOf(100.0);
    this.weightage = weightage != null ? weightage : BigDecimal.valueOf(10.0);
    this.dueDate = dueDate;
    this.attachmentUrl = attachmentUrl;
    this.published = published != null ? published : true;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_code", nullable = false, length = 40)
  private String courseCode;

  @Column(name = "section_code", length = 60)
  private String sectionCode;

  @Column(name = "faculty_id", nullable = false, length = 60)
  private String facultyId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "total_marks", nullable = false)
  private BigDecimal totalMarks = BigDecimal.valueOf(100.0);

  @Column(nullable = false)
  private BigDecimal weightage = BigDecimal.valueOf(10.0);

  @Column(name = "due_date", nullable = false)
  private OffsetDateTime dueDate;

  @Column(name = "attachment_url", length = 255)
  private String attachmentUrl;

  @Column(nullable = false)
  private Boolean published = true;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
