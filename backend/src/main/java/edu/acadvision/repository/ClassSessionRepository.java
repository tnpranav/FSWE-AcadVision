package edu.acadvision.repository;

import edu.acadvision.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
  List<ClassSession> findByCourseCodeOrderBySessionDateDesc(String courseCode);
  List<ClassSession> findByCourseCodeAndSectionCode(String courseCode, String sectionCode);
  List<ClassSession> findByFacultyIdOrderBySessionDateDesc(String facultyId);
  List<ClassSession> findByFacultyIdAndSessionDate(String facultyId, LocalDate sessionDate);
}
