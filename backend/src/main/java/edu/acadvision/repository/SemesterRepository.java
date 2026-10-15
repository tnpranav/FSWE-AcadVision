package edu.acadvision.repository;

import edu.acadvision.model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
  Optional<Semester> findByIsCurrentTrue();
  List<Semester> findByAcademicYearId(Long academicYearId);
}
