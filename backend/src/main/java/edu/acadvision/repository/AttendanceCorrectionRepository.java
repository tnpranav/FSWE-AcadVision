package edu.acadvision.repository;

import edu.acadvision.model.AttendanceCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, Long> {
  List<AttendanceCorrection> findByStudentIdOrderByCreatedAtDesc(String studentId);
  List<AttendanceCorrection> findByStatusOrderByCreatedAtDesc(String status);
}
