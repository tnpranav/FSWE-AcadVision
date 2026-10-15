package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/courses")
public class EnrollmentController {
  private final JdbcTemplate jdbc;
  public EnrollmentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping
  public List<Map<String,Object>> catalog(@AuthenticationPrincipal UserPrincipal principal) {
    requireStudent(principal);
    return jdbc.queryForList("""
        SELECT c.course_code AS code,c.title,c.department,c.credits,c.faculty_name AS faculty,
        cs.section_code,cs.max_capacity,cs.enrolled_count,e.status AS enrollment_status,
        COALESCE((SELECT ROUND(100.0*COUNT(*) FILTER (WHERE p.is_completed)/NULLIF(COUNT(*),0)) FROM course_modules m LEFT JOIN student_module_progress p ON p.module_id=m.id AND p.student_id=? WHERE m.course_code=c.course_code),0) AS completion
        FROM courses c LEFT JOIN course_sections cs ON cs.course_code=c.course_code
        LEFT JOIN semesters sem ON sem.id=cs.semester_id AND sem.is_current=TRUE
        LEFT JOIN enrollments e ON e.course_code=c.course_code AND e.section_code=cs.section_code AND e.student_id=? AND e.semester_id=sem.id
        WHERE c.active=TRUE AND (sem.is_current=TRUE OR cs.id IS NULL) ORDER BY c.course_code
        """, principal.getUserId(),principal.getUserId());
  }

  @PostMapping("/enroll")
  @Transactional
  public Map<String,Object> enroll(@Valid @RequestBody EnrollmentInput input,@AuthenticationPrincipal UserPrincipal principal) {
    requireStudent(principal);
    Long semester=jdbc.query("SELECT id FROM semesters WHERE is_current=TRUE AND registration_open=TRUE FOR UPDATE",rs->rs.next()?rs.getLong(1):null);
    if(semester==null) throw new ResponseStatusException(HttpStatus.CONFLICT,"Course registration is closed");
    List<Map<String,Object>> sections=jdbc.queryForList("SELECT cs.id,cs.course_code,cs.section_code,cs.max_capacity,cs.enrolled_count FROM course_sections cs JOIN courses c ON c.course_code=cs.course_code WHERE cs.section_code=? AND cs.course_code=? AND cs.semester_id=? AND c.active=TRUE FOR UPDATE",input.sectionCode(),input.courseCode(),semester);
    if(sections.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Course section is not available this semester");
    Map<String,Object> section=sections.getFirst();
    if(((Number)section.get("enrolled_count")).intValue()>=((Number)section.get("max_capacity")).intValue()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Course section is full");
    Integer missing=jdbc.queryForObject("SELECT COUNT(*) FROM course_prerequisites p JOIN grading_policies required ON required.grade_letter=p.min_grade WHERE p.course_code=? AND NOT EXISTS (SELECT 1 FROM student_grades g WHERE g.student_id=? AND g.course_code=p.prerequisite_course_code AND g.published=TRUE AND g.grade_point>=required.grade_point)",Integer.class,input.courseCode(),principal.getUserId());
    if(missing!=null&&missing>0) throw new ResponseStatusException(HttpStatus.CONFLICT,"Complete the required prerequisites before enrolling");
    Integer conflict=jdbc.queryForObject("""
        SELECT COUNT(*) FROM timetable_slots target JOIN timetable_slots existing ON existing.day_of_week=target.day_of_week
        JOIN course_sections enrolled ON enrolled.section_code=existing.section_code JOIN enrollments e ON e.section_code=enrolled.section_code
        WHERE target.section_code=? AND e.student_id=? AND e.semester_id=? AND e.status='ENROLLED'
        AND target.start_time<existing.end_time AND target.end_time>existing.start_time
        """,Integer.class,input.sectionCode(),principal.getUserId(),semester);
    if(conflict!=null&&conflict>0) throw new ResponseStatusException(HttpStatus.CONFLICT,"This section conflicts with your current timetable");
    try {
      List<Map<String,Object>> previous=jdbc.queryForList("SELECT id,status FROM enrollments WHERE student_id=? AND course_code=? AND semester_id=? FOR UPDATE",principal.getUserId(),input.courseCode(),semester);
      if(!previous.isEmpty()) {
        if("ENROLLED".equals(previous.getFirst().get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"You are already enrolled in this course");
        jdbc.update("UPDATE enrollments SET section_code=?,status='ENROLLED',drop_date=NULL,enrollment_date=CURRENT_DATE WHERE id=?",input.sectionCode(),previous.getFirst().get("id"));
      } else jdbc.update("INSERT INTO enrollments(student_id,course_code,section_code,semester_id,status) VALUES(?,?,?,?, 'ENROLLED')",principal.getUserId(),input.courseCode(),input.sectionCode(),semester);
      jdbc.update("UPDATE course_sections SET enrolled_count=enrolled_count+1 WHERE id=?",section.get("id"));
    } catch(org.springframework.dao.DataIntegrityViolationException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT,"You are already enrolled in this course for this semester"); }
    return Map.of("message","Enrollment completed","sectionCode",input.sectionCode());
  }

  @DeleteMapping("/{courseCode}")
  @Transactional
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void drop(@PathVariable String courseCode,@AuthenticationPrincipal UserPrincipal principal) {
    requireStudent(principal);
    Long semester=jdbc.query("SELECT id FROM semesters WHERE is_current=TRUE AND registration_open=TRUE FOR UPDATE",rs->rs.next()?rs.getLong(1):null);
    if(semester==null) throw new ResponseStatusException(HttpStatus.CONFLICT,"Course changes are closed");
    List<Map<String,Object>> rows=jdbc.queryForList("SELECT id,section_code FROM enrollments WHERE student_id=? AND course_code=? AND semester_id=? AND status='ENROLLED' FOR UPDATE",principal.getUserId(),courseCode,semester);
    if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Active enrollment not found");
    jdbc.update("UPDATE enrollments SET status='DROPPED',drop_date=CURRENT_DATE WHERE id=?",rows.getFirst().get("id"));
    if(rows.getFirst().get("section_code")!=null) jdbc.update("UPDATE course_sections SET enrolled_count=GREATEST(enrolled_count-1,0) WHERE section_code=?",rows.getFirst().get("section_code"));
  }

  private void requireStudent(UserPrincipal principal) {
    if(principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
    if(!"STUDENT".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Student access required");
  }
  public record EnrollmentInput(@NotBlank String courseCode,@NotBlank @Size(max=60) String sectionCode) {}
}
