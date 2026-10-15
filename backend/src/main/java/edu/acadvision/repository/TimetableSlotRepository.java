package edu.acadvision.repository;

import edu.acadvision.model.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
  List<TimetableSlot> findBySectionCode(String sectionCode);
  List<TimetableSlot> findByFacultyId(String facultyId);
  List<TimetableSlot> findBySectionCodeIn(List<String> sectionCodes);
}
