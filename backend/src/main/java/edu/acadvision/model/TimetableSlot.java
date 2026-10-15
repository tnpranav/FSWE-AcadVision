package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "timetable_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "section_code", nullable = false, length = 60)
  private String sectionCode;

  @Column(name = "day_of_week", nullable = false, length = 15)
  private String dayOfWeek; // MONDAY, TUESDAY, etc.

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "classroom_id")
  private Long classroomId;

  @Column(name = "faculty_id", length = 60)
  private String facultyId;
}
