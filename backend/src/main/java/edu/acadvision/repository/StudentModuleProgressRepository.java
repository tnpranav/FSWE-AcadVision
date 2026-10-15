package edu.acadvision.repository;

import edu.acadvision.model.StudentModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentModuleProgressRepository extends JpaRepository<StudentModuleProgress, Long> {
  List<StudentModuleProgress> findByStudentId(String studentId);
  Optional<StudentModuleProgress> findByStudentIdAndModuleId(String studentId, Long moduleId);
}
