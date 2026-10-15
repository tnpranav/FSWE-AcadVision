package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {
  private final JdbcTemplate jdbc;
  public PortalController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    String id = principal.getUserId();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("profile", principal.getRole().equals("STUDENT")
        ? one("SELECT s.student_id AS id,s.full_name AS name,s.email,s.department,s.semester,s.cgpa,s.attendance,s.status,s.hobbies,s.earned_credits AS credits,COALESCE(p.total_credits_required,120) AS credits_required FROM students s LEFT JOIN programs p ON p.code=s.program_code WHERE s.student_id=?", id)
        : principal.getRole().equals("FACULTY")
            ? one("SELECT faculty_id AS id, full_name AS name, email, department, designation, qualification, office_room FROM faculty WHERE faculty_id=?", id)
            : Map.of("id", id, "name", principal.getFullName(), "email", principal.getEmail(), "role", principal.getRole()));

    if ("STUDENT".equals(principal.getRole())) {
      result.put("courses", jdbc.queryForList("""
          SELECT c.course_code AS code,c.title,c.faculty_name AS faculty,c.credits,
          COUNT(DISTINCT cm.id) AS module_count,
          COUNT(DISTINCT CASE WHEN smp.is_completed THEN cm.id END) AS completed_modules,
          CASE WHEN COUNT(DISTINCT cm.id)=0 THEN 0 ELSE ROUND(100.0*COUNT(DISTINCT CASE WHEN smp.is_completed THEN cm.id END)/COUNT(DISTINCT cm.id)) END AS completion,
          COUNT(DISTINCT ar.id) AS sessions,
          COUNT(DISTINCT CASE WHEN ar.status IN ('PRESENT','LATE','EXCUSED') THEN ar.id END) AS attended_sessions
          FROM enrollments e JOIN courses c ON c.course_code=e.course_code
          LEFT JOIN course_modules cm ON cm.course_code=c.course_code
          LEFT JOIN student_module_progress smp ON smp.module_id=cm.id AND smp.student_id=e.student_id
          LEFT JOIN attendance_records ar ON ar.course_code=c.course_code AND ar.student_id=e.student_id
          WHERE e.student_id=? AND e.status='ENROLLED' GROUP BY c.course_code,c.title,c.faculty_name,c.credits ORDER BY c.course_code
          """, id));
      result.put("attendance", jdbc.queryForList("SELECT ar.id,ar.course_code AS code,c.title,ar.class_date,ar.status,ar.remarks FROM attendance_records ar JOIN courses c ON c.course_code=ar.course_code WHERE ar.student_id=? ORDER BY ar.class_date DESC", id));
      result.put("grades", jdbc.queryForList("SELECT g.course_code AS code,c.title,g.total_score,g.letter_grade,g.grade_point,g.credits_earned,g.finalized_at FROM student_grades g JOIN courses c ON c.course_code=g.course_code WHERE g.student_id=? AND g.published=TRUE ORDER BY g.finalized_at DESC", id));
      result.put("gpaHistory", jdbc.queryForList("SELECT semester_number,semester_gpa,cumulative_cgpa,credits_attempted,credits_earned FROM gpa_records WHERE student_id=? ORDER BY semester_number", id));
      result.put("fees", jdbc.queryForList("SELECT invoice_id AS id,description,due_date,amount,net_payable,status,fee_type FROM fee_invoices WHERE student_id=? ORDER BY due_date", id));
      result.put("timetable", jdbc.queryForList("""
          SELECT ts.day_of_week,ts.start_time,ts.end_time,c.course_code AS code,c.title,
          f.full_name AS faculty,cl.room_number,cl.building FROM enrollments e
          JOIN course_sections cs ON cs.section_code=e.section_code JOIN timetable_slots ts ON ts.section_code=cs.section_code
          JOIN courses c ON c.course_code=cs.course_code LEFT JOIN faculty f ON f.faculty_id=cs.faculty_id
          LEFT JOIN classrooms cl ON cl.id=ts.classroom_id WHERE e.student_id=? AND e.status='ENROLLED'
          ORDER BY ts.day_of_week,ts.start_time
          """, id));
      result.put("assignments", jdbc.queryForList("""
          SELECT a.id,a.course_code AS code,c.title AS course,a.title,a.description,a.total_marks,a.due_date,
          s.status AS submission_status,s.score,s.feedback FROM enrollments e JOIN assignments a ON a.course_code=e.course_code
          JOIN courses c ON c.course_code=a.course_code LEFT JOIN assignment_submissions s ON s.assignment_id=a.id AND s.student_id=e.student_id
          WHERE e.student_id=? AND a.published=TRUE ORDER BY a.due_date
          """, id));
    } else if ("FACULTY".equals(principal.getRole())) {
      result.put("courses", jdbc.queryForList("""
          SELECT c.course_code AS code,c.title,c.faculty_name AS faculty,c.credits,
          COUNT(DISTINCT e.student_id) AS students,
          COUNT(DISTINCT cm.id) AS module_count,
          COUNT(DISTINCT CASE WHEN smp.is_completed THEN (e.student_id || ':' || cm.id::text) END) AS completed_modules,
          CASE WHEN COUNT(DISTINCT e.student_id || ':' || cm.id::text)=0 THEN 0
          ELSE ROUND(100.0*COUNT(DISTINCT CASE WHEN smp.is_completed THEN e.student_id || ':' || cm.id::text END)/COUNT(DISTINCT e.student_id || ':' || cm.id::text)) END AS completion
          FROM courses c LEFT JOIN enrollments e ON e.course_code=c.course_code AND e.status='ENROLLED'
          LEFT JOIN course_modules cm ON cm.course_code=c.course_code
          LEFT JOIN student_module_progress smp ON smp.student_id=e.student_id AND smp.module_id=cm.id
          WHERE c.primary_faculty_id=? GROUP BY c.course_code,c.title,c.faculty_name,c.credits ORDER BY c.course_code
          """, id));
      result.put("students", jdbc.queryForList("""
          SELECT DISTINCT s.student_id AS id,s.full_name AS name,s.email,s.department,s.semester,s.cgpa,s.attendance,s.status,s.hobbies,
          c.course_code AS course_code,c.title AS course_title FROM students s JOIN enrollments e ON e.student_id=s.student_id
          JOIN courses c ON c.course_code=e.course_code WHERE c.primary_faculty_id=? AND e.status='ENROLLED' ORDER BY s.full_name
          """, id));
      result.put("attendance", jdbc.queryForList("SELECT ar.id,ar.student_id AS student_id,s.full_name AS student_name,ar.course_code AS code,c.title,ar.class_date,ar.status FROM attendance_records ar JOIN students s ON s.student_id=ar.student_id JOIN courses c ON c.course_code=ar.course_code WHERE c.primary_faculty_id=? ORDER BY ar.class_date DESC", id));
      result.put("timetable", jdbc.queryForList("""
          SELECT ts.day_of_week,ts.start_time,ts.end_time,c.course_code AS code,c.title,cs.section_code,
          cl.room_number,cl.building FROM timetable_slots ts JOIN course_sections cs ON cs.section_code=ts.section_code
          JOIN courses c ON c.course_code=cs.course_code LEFT JOIN classrooms cl ON cl.id=ts.classroom_id
          WHERE cs.faculty_id=? ORDER BY ts.day_of_week,ts.start_time
          """, id));
      result.put("assignments", jdbc.queryForList("SELECT a.id,a.course_code AS code,c.title AS course,a.title,a.description,a.total_marks,a.weightage,a.due_date,COUNT(s.id) FILTER (WHERE s.status IN ('SUBMITTED','LATE')) AS pending_count FROM assignments a JOIN courses c ON c.course_code=a.course_code LEFT JOIN assignment_submissions s ON s.assignment_id=a.id WHERE a.faculty_id=? GROUP BY a.id,c.title ORDER BY a.due_date", id));
    } else {
      result.put("courses", List.of()); result.put("students", List.of()); result.put("attendance", List.of());
      result.put("timetable", List.of()); result.put("assignments", List.of());
    }
    if ("ADMIN".equals(principal.getRole())) result.put("attendance", jdbc.queryForList("SELECT * FROM attendance_records ORDER BY class_date DESC LIMIT 500"));
    result.put("announcements", jdbc.queryForList("""
        SELECT a.id,a.title,a.content AS detail,a.target_audience,a.priority,a.is_pinned,a.created_at AS time
        FROM announcements a WHERE a.target_audience='ALL' OR a.target_audience=? ORDER BY a.is_pinned DESC,a.created_at DESC LIMIT 100
        """, principal.getRole().equals("STUDENT") ? "STUDENTS" : principal.getRole().equals("FACULTY") ? "FACULTY" : "ALL"));
    result.put("notifications", jdbc.queryForList("SELECT id,title,message,notification_type,link_url,read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 100", id));
    result.put("supportTickets", "ADMIN".equals(principal.getRole())
        ? jdbc.queryForList("SELECT ticket_number,user_id,category,subject,description,priority,status,created_at FROM support_tickets ORDER BY created_at DESC LIMIT 200")
        : jdbc.queryForList("SELECT ticket_number,user_id,category,subject,description,priority,status,created_at FROM support_tickets WHERE user_id=? ORDER BY created_at DESC LIMIT 100",id));
    return result;
  }

  private Map<String, Object> one(String sql, Object... args) {
    List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
    return rows.isEmpty() ? Map.of() : rows.getFirst();
  }
}
