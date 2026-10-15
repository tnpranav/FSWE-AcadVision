package edu.acadvision.repository;

import edu.acadvision.model.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {
  List<StudentGrade> findByStudentId(String studentId);
  List<StudentGrade> findByCourseCode(String courseCode);
  Optional<StudentGrade> findByStudentIdAndCourseCodeAndSemesterId(String studentId, String courseCode, Long semesterId);
}
