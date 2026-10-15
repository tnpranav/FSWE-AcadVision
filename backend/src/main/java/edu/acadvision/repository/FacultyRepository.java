package edu.acadvision.repository;
import edu.acadvision.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface FacultyRepository extends JpaRepository<Faculty,Long> {
  Optional<Faculty> findByFacultyId(String facultyId);
}
