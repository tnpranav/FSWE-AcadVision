package edu.acadvision.repository;

import edu.acadvision.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
  Optional<Classroom> findByRoomNumber(String roomNumber);
}
