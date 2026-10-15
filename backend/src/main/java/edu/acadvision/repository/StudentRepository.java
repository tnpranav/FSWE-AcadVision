package edu.acadvision.repository;
import edu.acadvision.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student,Long> {
  Optional<Student> findByStudentId(String studentId);
  List<Student> findByFullNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(String name, String studentId);
}
