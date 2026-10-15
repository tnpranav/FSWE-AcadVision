package edu.acadvision.controller;

import edu.acadvision.security.user.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class CommunicationController {
  private final JdbcTemplate jdbc;
  public CommunicationController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/api/announcements")
  public List<Map<String,Object>> announcements(@AuthenticationPrincipal UserPrincipal principal) {
    String audience = principal.getRole().equals("STUDENT") ? "STUDENTS" : principal.getRole().equals("FACULTY") ? "FACULTY" : "ALL";
    return jdbc.queryForList("SELECT id,title,content,target_audience,department_code,author_id,priority,is_pinned,created_at FROM announcements WHERE target_audience IN ('ALL',?) ORDER BY is_pinned DESC,created_at DESC", audience);
  }

  @PostMapping("/api/admin/announcements")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> createAnnouncement(@Valid @RequestBody AnnouncementInput input, @AuthenticationPrincipal UserPrincipal principal) {
    if(!"ADMIN".equals(principal.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");
    jdbc.update("INSERT INTO announcements(title,content,target_audience,author_id,priority,is_pinned) VALUES(?,?,?,?,?,?)",input.title(),input.content(),input.audience(),principal.getUserId(),input.priority(),input.pinned());
    return Map.of("message","Announcement published");
  }

  @GetMapping("/api/notifications")
  public List<Map<String,Object>> notifications(@AuthenticationPrincipal UserPrincipal principal) {
    return jdbc.queryForList("SELECT id,title,message,notification_type,link_url,read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 100",principal.getUserId());
  }

  @PatchMapping("/api/notifications/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@PathVariable long id,@AuthenticationPrincipal UserPrincipal principal) {
    int updated=jdbc.update("UPDATE notifications SET read=TRUE WHERE id=? AND user_id=?",id,principal.getUserId());
    if(updated==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found");
  }

  @PostMapping("/api/support")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> createTicket(@Valid @RequestBody TicketInput input,@AuthenticationPrincipal UserPrincipal principal) {
    String number="AV-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
    jdbc.update("INSERT INTO support_tickets(ticket_number,user_id,category,subject,description,priority) VALUES(?,?,?,?,?,?)",number,principal.getUserId(),input.category(),input.subject(),input.description(),input.priority());
    return Map.of("ticketNumber",number,"status","OPEN");
  }

  @GetMapping("/api/support")
  public List<Map<String,Object>> tickets(@AuthenticationPrincipal UserPrincipal principal) {
    if("ADMIN".equals(principal.getRole())) return jdbc.queryForList("SELECT ticket_number, user_id, category, subject, description, priority, status, created_at, resolved_at FROM support_tickets ORDER BY created_at DESC");
    return jdbc.queryForList("SELECT ticket_number, user_id, category, subject, description, priority, status, created_at, resolved_at FROM support_tickets WHERE user_id=? ORDER BY created_at DESC",principal.getUserId());
  }

  public record AnnouncementInput(@NotBlank @Size(max=200) String title,@NotBlank @Size(max=10000) String content,
      @Pattern(regexp="ALL|STUDENTS|FACULTY") String audience,@Pattern(regexp="LOW|NORMAL|HIGH|URGENT") String priority,boolean pinned) {}
  public record TicketInput(@Pattern(regexp="ACADEMIC|ATTENDANCE|FEE_PAYMENT|TIMETABLE|TECHNICAL") String category,
      @NotBlank @Size(max=200) String subject,@NotBlank @Size(max=10000) String description,
      @Pattern(regexp="LOW|MEDIUM|HIGH") String priority) {}
}
