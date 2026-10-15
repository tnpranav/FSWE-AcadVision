package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_number", nullable = false, unique = true, length = 40)
  private String roomNumber;

  @Column(nullable = false, length = 80)
  private String building;

  @Column(nullable = false)
  @Builder.Default
  private Integer capacity = 60;

  @Column(name = "room_type", nullable = false, length = 40)
  @Builder.Default
  private String roomType = "LECTURE_HALL";
}
