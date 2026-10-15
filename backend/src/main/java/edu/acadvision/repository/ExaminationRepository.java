package edu.acadvision.repository;

import edu.acadvision.model.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {
  List<Examination> findByCourseCodeOrderByExamDateAsc(String courseCode);
  List<Examination> findBySemesterIdOrderByExamDateAsc(Long semesterId);
  List<Examination> findByCourseCodeInOrderByExamDateAsc(List<String> courseCodes);
}
