package edu.acadvision.repository;

import edu.acadvision.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
  List<Assignment> findByCourseCodeOrderByDueDateDesc(String courseCode);
  List<Assignment> findByFacultyIdOrderByDueDateDesc(String facultyId);
  List<Assignment> findByCourseCodeInOrderByDueDateDesc(List<String> courseCodes);
}
