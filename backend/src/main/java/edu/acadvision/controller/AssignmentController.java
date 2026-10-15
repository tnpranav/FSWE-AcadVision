package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class AssignmentController {
  private final JdbcTemplate jdbc;
  public AssignmentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/api/faculty/assignments")
  public List<Map<String,Object>> facultyAssignments(@AuthenticationPrincipal UserPrincipal principal) {
    requireRole(principal, "FACULTY");
    return jdbc.queryForList("SELECT a.id,a.course_code AS code,c.title AS course,a.title,a.description,a.total_marks,a.weightage,a.due_date,a.published,COUNT(s.id) FILTER (WHERE s.status IN ('SUBMITTED','LATE')) AS pending_count FROM assignments a JOIN courses c ON c.course_code=a.course_code LEFT JOIN assignment_submissions s ON s.assignment_id=a.id WHERE a.faculty_id=? GROUP BY a.id,c.title ORDER BY a.due_date", principal.getUserId());
  }

  @GetMapping("/api/student/assignments")
  public List<Map<String,Object>> studentAssignments(@AuthenticationPrincipal UserPrincipal principal) {
    requireRole(principal, "STUDENT");
    return jdbc.queryForList("SELECT a.id,a.course_code AS code,c.title AS course,a.title,a.description,a.total_marks,a.due_date,s.status AS submission_status,s.score,s.feedback,s.submission_date FROM enrollments e JOIN assignments a ON a.course_code=e.course_code JOIN courses c ON c.course_code=a.course_code LEFT JOIN assignment_submissions s ON s.assignment_id=a.id AND s.student_id=e.student_id WHERE e.student_id=? AND e.status='ENROLLED' AND a.published=TRUE ORDER BY a.due_date", principal.getUserId());
  }

  @PostMapping("/api/faculty/assignments")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> create(@Valid @RequestBody AssignmentInput input, @AuthenticationPrincipal UserPrincipal principal) {
    requireRole(principal, "FACULTY");
    Integer assigned = jdbc.queryForObject("SELECT COUNT(*) FROM courses WHERE course_code=? AND primary_faculty_id=?", Integer.class, input.courseCode(), principal.getUserId());
    if (assigned == null || assigned == 0) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Course is not assigned to this faculty account");
    jdbc.update("INSERT INTO assignments(course_code,section_code,faculty_id,title,description,total_marks,weightage,due_date,published) VALUES(?,?,?,?,?,?,?,?,TRUE)", input.courseCode(),input.sectionCode(),principal.getUserId(),input.title().trim(),input.description(),input.totalMarks(),input.weightage(),input.dueDate());
    return Map.of("message","Assignment created");
  }

  @PostMapping("/api/student/assignments/{assignmentId}/submit")
  @Transactional
  public Map<String,Object> submit(@PathVariable long assignmentId, @Valid @RequestBody SubmissionInput input, @AuthenticationPrincipal UserPrincipal principal) {
    requireRole(principal, "STUDENT");
    List<Map<String,Object>> eligible = jdbc.queryForList("SELECT a.id,CASE WHEN a.due_date<CURRENT_TIMESTAMP THEN 'LATE' ELSE 'SUBMITTED' END AS submission_status FROM assignments a JOIN enrollments e ON e.course_code=a.course_code AND e.student_id=? AND e.status='ENROLLED' WHERE a.id=? AND a.published=TRUE", principal.getUserId(), assignmentId);
    if (eligible.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Assignment not found for your enrolled courses");
    String status = (String) eligible.getFirst().get("submission_status");
    jdbc.update("INSERT INTO assignment_submissions(assignment_id,student_id,content,status) VALUES(?,?,?,?) ON CONFLICT (assignment_id,student_id) DO UPDATE SET content=EXCLUDED.content,submission_date=CURRENT_TIMESTAMP,status=EXCLUDED.status,score=NULL,feedback=NULL,graded_by=NULL,graded_at=NULL", assignmentId,principal.getUserId(),input.content(),status);
    return Map.of("message","Submission saved","status",status);
  }

  @PatchMapping("/api/faculty/assignments/{assignmentId}/submissions/{studentId}")
  @Transactional
  public Map<String,Object> grade(@PathVariable long assignmentId, @PathVariable String studentId, @Valid @RequestBody GradeInput input, @AuthenticationPrincipal UserPrincipal principal) {
    requireRole(principal, "FACULTY");
    List<Map<String,Object>> rows = jdbc.queryForList("SELECT a.total_marks FROM assignments a WHERE a.id=? AND a.faculty_id=?", assignmentId,principal.getUserId());
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Assignment not found for your courses");
    BigDecimal maximum = (BigDecimal) rows.getFirst().get("total_marks");
    if (input.score().compareTo(maximum) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Score cannot exceed the assignment maximum");
    int changed = jdbc.update("UPDATE assignment_submissions SET score=?,feedback=?,status='GRADED',graded_by=?,graded_at=CURRENT_TIMESTAMP WHERE assignment_id=? AND student_id=?",input.score(),input.feedback(),principal.getUserId(),assignmentId,studentId);
    if(changed==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Student submission not found");
    return Map.of("message","Submission graded");
  }

  private void requireRole(UserPrincipal principal, String role) {
    if(principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
    if(!role.equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This action is not permitted for your role");
  }
  public record AssignmentInput(@NotBlank String courseCode, @Size(max=60) String sectionCode, @NotBlank @Size(max=200) String title,
      @Size(max=10000) String description, @NotNull @DecimalMin("0.01") BigDecimal totalMarks, @NotNull @DecimalMin("0.01") BigDecimal weightage,
      @NotNull @Future OffsetDateTime dueDate) {}
  public record SubmissionInput(@NotBlank @Size(max=10000) String content) {}
  public record GradeInput(@NotNull @DecimalMin("0.00") BigDecimal score, @Size(max=1000) String feedback) {}
}
