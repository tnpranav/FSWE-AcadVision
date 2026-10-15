package edu.acadvision.repository;

import edu.acadvision.model.GpaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GpaRecordRepository extends JpaRepository<GpaRecord, Long> {
  List<GpaRecord> findByStudentIdOrderBySemesterNumberAsc(String studentId);
}
