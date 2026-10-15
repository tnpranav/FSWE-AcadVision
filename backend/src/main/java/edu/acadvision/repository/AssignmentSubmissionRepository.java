package edu.acadvision.repository;

import edu.acadvision.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
  List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
  List<AssignmentSubmission> findByStudentIdOrderBySubmissionDateDesc(String studentId);
  Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, String studentId);
}
