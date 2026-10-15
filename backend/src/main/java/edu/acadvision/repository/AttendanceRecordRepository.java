package edu.acadvision.repository;

import edu.acadvision.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
  List<AttendanceRecord> findByStudentIdOrderByClassDateDesc(String studentId);
  List<AttendanceRecord> findByCourseCodeOrderByClassDateDesc(String courseCode);
  List<AttendanceRecord> findByCourseCodeAndClassDate(String courseCode, LocalDate classDate);
  Optional<AttendanceRecord> findByStudentIdAndCourseCodeAndClassDate(String studentId, String courseCode, LocalDate classDate);
}
