package edu.acadvision.repository;

import edu.acadvision.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  List<Enrollment> findByStudentId(String studentId);
  List<Enrollment> findByStudentIdAndStatus(String studentId, String status);
  List<Enrollment> findByCourseCode(String courseCode);
  List<Enrollment> findBySectionCode(String sectionCode);
  Optional<Enrollment> findByStudentIdAndCourseCodeAndSemesterId(String studentId, String courseCode, Long semesterId);
  boolean existsByStudentIdAndCourseCodeAndStatus(String studentId, String courseCode, String status);
}
