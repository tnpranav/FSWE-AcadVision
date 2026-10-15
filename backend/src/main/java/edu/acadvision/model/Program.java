package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String code;

  @Column(nullable = false, length = 180)
  private String name;

  @Column(name = "department_code", nullable = false, length = 20)
  private String departmentCode;

  @Column(name = "degree_level", nullable = false, length = 20)
  @Builder.Default
  private String degreeLevel = "UNDERGRADUATE";

  @Column(name = "total_semesters", nullable = false)
  @Builder.Default
  private Integer totalSemesters = 8;

  @Column(name = "total_credits_required", nullable = false)
  @Builder.Default
  private Integer totalCreditsRequired = 160;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
