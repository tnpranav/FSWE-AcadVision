package edu.acadvision.repository;

import edu.acadvision.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
  Optional<CourseSection> findBySectionCode(String sectionCode);
  List<CourseSection> findByCourseCode(String courseCode);
  List<CourseSection> findByFacultyId(String facultyId);
}
