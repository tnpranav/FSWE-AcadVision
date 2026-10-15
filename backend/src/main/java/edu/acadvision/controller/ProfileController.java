package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class ProfileController {
  private final JdbcTemplate jdbc;
  public ProfileController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/api/student/profile")
  public Map<String,Object> student(@AuthenticationPrincipal UserPrincipal principal) {
    require(principal,"STUDENT");
    return one("SELECT student_id AS id,full_name AS name,email,phone,department,semester,batch_year,hobbies,extracurriculars FROM students WHERE student_id=?",principal.getUserId());
  }

  @PutMapping("/api/student/profile")
  public Map<String,Object> updateStudent(@Valid @RequestBody StudentProfile input,@AuthenticationPrincipal UserPrincipal principal) {
    require(principal,"STUDENT");
    jdbc.update("UPDATE students SET phone=?,hobbies=?,extracurriculars=?,updated_at=CURRENT_TIMESTAMP WHERE student_id=?",input.phone(),input.hobbies(),input.extracurriculars(),principal.getUserId());
    return student(principal);
  }

  @GetMapping("/api/faculty/profile")
  public Map<String,Object> faculty(@AuthenticationPrincipal UserPrincipal principal) {
    require(principal,"FACULTY");
    return one("SELECT faculty_id AS id,full_name AS name,email,phone,department,designation,office_room,qualification,specialization FROM faculty WHERE faculty_id=?",principal.getUserId());
  }

  @PutMapping("/api/faculty/profile")
  public Map<String,Object> updateFaculty(@Valid @RequestBody FacultyProfile input,@AuthenticationPrincipal UserPrincipal principal) {
    require(principal,"FACULTY");
    jdbc.update("UPDATE faculty SET phone=?,office_room=?,qualification=?,specialization=?,updated_at=CURRENT_TIMESTAMP WHERE faculty_id=?",input.phone(),input.officeRoom(),input.qualification(),input.specialization(),principal.getUserId());
    return faculty(principal);
  }

  private Map<String,Object> one(String sql,String id) {
    List<Map<String,Object>> rows=jdbc.queryForList(sql,id);
    if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Profile not found");
    return rows.getFirst();
  }
  private void require(UserPrincipal principal,String role) {
    if(principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
    if(!role.equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Profile access is not permitted");
  }
  public record StudentProfile(@Size(max=30) String phone,@Size(max=4000) String hobbies,@Size(max=4000) String extracurriculars) {}
  public record FacultyProfile(@Size(max=30) String phone,@Size(max=40) String officeRoom,@Size(max=120) String qualification,@Size(max=160) String specialization) {}
}
