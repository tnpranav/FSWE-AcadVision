package edu.acadvision.repository;

import edu.acadvision.model.AssessmentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentMarkRepository extends JpaRepository<AssessmentMark, Long> {
  List<AssessmentMark> findByExaminationId(Long examinationId);
  List<AssessmentMark> findByStudentId(String studentId);
  Optional<AssessmentMark> findByExaminationIdAndStudentId(Long examinationId, String studentId);
}
