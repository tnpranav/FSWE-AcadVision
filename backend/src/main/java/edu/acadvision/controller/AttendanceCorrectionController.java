package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class AttendanceCorrectionController {
  private final JdbcTemplate jdbc;
  public AttendanceCorrectionController(JdbcTemplate jdbc) { this.jdbc=jdbc; }

  @PostMapping("/api/student/attendance/{recordId}/corrections")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> request(@PathVariable long recordId,@Valid @RequestBody CorrectionInput input,@AuthenticationPrincipal UserPrincipal principal) {
    require(principal,"STUDENT");
    Integer owns=jdbc.queryForObject("SELECT COUNT(*) FROM attendance_records WHERE id=? AND student_id=?",Integer.class,recordId,principal.getUserId());
    if(owns==null||owns==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Attendance record not found");
    Integer pending=jdbc.queryForObject("SELECT COUNT(*) FROM attendance_corrections WHERE attendance_record_id=? AND status='PENDING'",Integer.class,recordId);
    if(pending!=null&&pending>0) throw new ResponseStatusException(HttpStatus.CONFLICT,"A correction request is already pending for this record");
    jdbc.update("INSERT INTO attendance_corrections(attendance_record_id,student_id,requested_status,reason) VALUES(?,?,?,?)",recordId,principal.getUserId(),input.requestedStatus(),input.reason());
    return Map.of("message","Attendance correction request submitted","status","PENDING");
  }

  @GetMapping("/api/admin/attendance-corrections")
  public List<Map<String,Object>> all(@AuthenticationPrincipal UserPrincipal principal) {
    if(principal==null||!"ADMIN".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");
    return jdbc.queryForList("SELECT ac.id,ac.attendance_record_id,ac.student_id,s.full_name AS student_name,ar.course_code,ar.class_date,ar.status AS recorded_status,ac.requested_status,ac.reason,ac.status,ac.created_at FROM attendance_corrections ac JOIN attendance_records ar ON ar.id=ac.attendance_record_id JOIN students s ON s.student_id=ac.student_id ORDER BY ac.created_at DESC");
  }

  @PatchMapping("/api/admin/attendance-corrections/{id}")
  @Transactional
  public Map<String,Object> review(@PathVariable long id,@Valid @RequestBody ReviewInput input,@AuthenticationPrincipal UserPrincipal principal) {
    if(principal==null||!"ADMIN".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");
    List<Map<String,Object>> rows=jdbc.queryForList("SELECT attendance_record_id,requested_status,status FROM attendance_corrections WHERE id=? FOR UPDATE",id);
    if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Correction request not found");
    if(!"PENDING".equals(rows.getFirst().get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Correction request has already been reviewed");
    jdbc.update("UPDATE attendance_corrections SET status=?,reviewed_by=?,reviewed_at=CURRENT_TIMESTAMP,reviewer_remarks=? WHERE id=?",input.status(),principal.getUserId(),input.remarks(),id);
    if("APPROVED".equals(input.status())) jdbc.update("UPDATE attendance_records SET status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",rows.getFirst().get("requested_status"),rows.getFirst().get("attendance_record_id"));
    return Map.of("message","Correction request reviewed","status",input.status());
  }

  private void require(UserPrincipal principal,String role) {
    if(principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
    if(!role.equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This action is not permitted for your role");
  }
  public record CorrectionInput(@Pattern(regexp="PRESENT|EXCUSED") String requestedStatus,@NotBlank @Size(max=2000) String reason) {}
  public record ReviewInput(@NotBlank @Pattern(regexp="APPROVED|REJECTED") String status,@Size(max=1000) String remarks) {}
}
