package edu.acadvision.controller;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import edu.acadvision.security.user.UserPrincipal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/faculty/attendance")
public class AttendanceController {
  private final JdbcTemplate jdbc;
  public AttendanceController(JdbcTemplate jdbc){this.jdbc=jdbc;}
  @GetMapping public List<Map<String,Object>> list(@RequestParam(required=false) String studentId, @AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    if ("ADMIN".equals(principal.getRole())) {
      if(studentId==null||studentId.isBlank()) return jdbc.queryForList("SELECT * FROM attendance_records ORDER BY class_date DESC");
      return jdbc.queryForList("SELECT * FROM attendance_records WHERE student_id = ? ORDER BY class_date DESC",studentId);
    }
    if (!"FACULTY".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    String sql = "SELECT ar.* FROM attendance_records ar JOIN courses c ON c.course_code=ar.course_code WHERE c.primary_faculty_id=?";
    return studentId == null || studentId.isBlank()
        ? jdbc.queryForList(sql + " ORDER BY ar.class_date DESC", principal.getUserId())
        : jdbc.queryForList(sql + " AND ar.student_id=? ORDER BY ar.class_date DESC", principal.getUserId(), studentId);
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> mark(@RequestBody AttendanceInput input, @AuthenticationPrincipal UserPrincipal principal) {
    if(principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
    if(!"FACULTY".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Faculty access required");
    if(input.studentId()==null||input.courseCode()==null||input.status()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"studentId, courseCode and status are required");
    String status=input.status().toUpperCase();
    if(!List.of("PRESENT","ABSENT","LATE","EXCUSED").contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid attendance status");
    Integer assigned=jdbc.queryForObject("SELECT COUNT(*) FROM courses WHERE course_code=? AND primary_faculty_id=?",Integer.class,input.courseCode(),principal.getUserId());
    if(assigned==null||assigned==0) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Course is not assigned to this faculty account");
    Integer enrolled=jdbc.queryForObject("SELECT COUNT(*) FROM enrollments WHERE student_id=? AND course_code=? AND status='ENROLLED'",Integer.class,input.studentId(),input.courseCode());
    if(enrolled==null||enrolled==0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Student is not enrolled in this course");
    try {
      jdbc.update("INSERT INTO attendance_records(student_id,course_code,class_date,status,marked_by) VALUES(?,?,?,?,?)",
        input.studentId(),input.courseCode(),input.classDate()==null?LocalDate.now():input.classDate(),status,principal.getUserId());
      return Map.of("message","Attendance recorded","studentId",input.studentId(),"status",status);
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,"Attendance already exists or referenced student/course is invalid");
    }
  }
  public record AttendanceInput(String studentId,String courseCode,LocalDate classDate,String status){}
}
