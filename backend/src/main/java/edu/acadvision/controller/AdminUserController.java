package edu.acadvision.controller;

import edu.acadvision.model.AppUser;
import edu.acadvision.model.AuditLog;
import edu.acadvision.repository.AppUserRepository;
import edu.acadvision.repository.AuditLogRepository;
import edu.acadvision.repository.RefreshTokenRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
  private final AppUserRepository users;
  private final PasswordEncoder passwords;
  private final AuditLogRepository audit;
  private final RefreshTokenRepository refreshTokens;
  private final JdbcTemplate jdbc;

  public AdminUserController(AppUserRepository users, PasswordEncoder passwords, AuditLogRepository audit, RefreshTokenRepository refreshTokens, JdbcTemplate jdbc) {
    this.users = users;
    this.passwords = passwords;
    this.audit = audit;
    this.refreshTokens = refreshTokens;
    this.jdbc = jdbc;
  }

  @GetMapping
  public List<UserView> list(@RequestParam(required = false) String q) {
    return users.findAll().stream()
        .filter(user -> q == null || q.isBlank() || (user.getUserId() + " " + user.getFullName() + " " + user.getEmail())
            .toLowerCase().contains(q.toLowerCase()))
        .map(UserView::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public UserView create(@Valid @RequestBody CreateUser input) {
    // Admin privilege changes require an explicitly authorized super-admin workflow.
    if ("ADMIN".equals(input.role())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator accounts require a super-administrator workflow");
    if (users.existsByUserId(input.userId()) || users.existsByEmail(input.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User ID or email is already in use");
    }
    AppUser saved = users.save(AppUser.builder()
        .userId(input.userId().trim()).fullName(input.fullName().trim()).email(input.email().trim().toLowerCase())
        .passwordHash(passwords.encode(input.initialPassword())).role(input.role())
        .active(true).mustChangePassword(true).mfaEnabled(false).build());
    String department=input.department()==null||input.department().isBlank()?"Unassigned":input.department().trim();
    if("STUDENT".equals(input.role())) {
      jdbc.update("INSERT INTO students(student_id,user_id,full_name,email,department) VALUES(?,?,?,?,?) ON CONFLICT(student_id) DO UPDATE SET user_id=EXCLUDED.user_id,full_name=EXCLUDED.full_name,email=EXCLUDED.email",input.userId().trim(),input.userId().trim(),input.fullName().trim(),input.email().trim().toLowerCase(),department);
    } else {
      jdbc.update("INSERT INTO faculty(faculty_id,user_id,full_name,email,department) VALUES(?,?,?,?,?) ON CONFLICT(faculty_id) DO UPDATE SET user_id=EXCLUDED.user_id,full_name=EXCLUDED.full_name,email=EXCLUDED.email",input.userId().trim(),input.userId().trim(),input.fullName().trim(),input.email().trim().toLowerCase(),department);
    }
    record("USER_CREATED", saved.getUserId(), "Role " + saved.getRole());
    return UserView.from(saved);
  }

  @PatchMapping("/{userId}/status")
  public UserView setStatus(@PathVariable String userId, @Valid @RequestBody StatusUpdate input) {
    AppUser user = find(userId);
    user.setActive(input.active());
    if (!input.active()) refreshTokens.findAll().stream()
        .filter(token -> token.getUserId().equals(userId) && !Boolean.TRUE.equals(token.getRevoked()))
        .forEach(token -> { token.setRevoked(true); refreshTokens.save(token); });
    AppUser saved = users.save(user);
    record(input.active() ? "ACCOUNT_ACTIVATED" : "ACCOUNT_DEACTIVATED", userId, "Account status changed");
    return UserView.from(saved);
  }

  @PatchMapping("/{userId}/force-password-change")
  public UserView forcePasswordChange(@PathVariable String userId) {
    AppUser user = find(userId);
    user.setMustChangePassword(true);
    AppUser saved = users.save(user);
    record("PASSWORD_CHANGE_REQUIRED", userId, "Account flagged to change password at next login");
    return UserView.from(saved);
  }

  private AppUser find(String userId) {
    return users.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private void record(String action, String userId, String details) {
    String actor = SecurityContextHolder.getContext().getAuthentication().getName();
    audit.save(AuditLog.builder().actorUserId(actor).action(action).entityType("APP_USER").entityId(userId).details(details).build());
  }

  public record CreateUser(
      @NotBlank @Size(max = 60) String userId,
      @NotBlank @Size(max = 160) String fullName,
      @NotBlank @Email @Size(max = 180) String email,
      @NotBlank @Size(min = 12, max = 72) String initialPassword,
      @NotBlank @Pattern(regexp = "STUDENT|FACULTY") String role,
      @Size(max=120) String department) {}
  public record StatusUpdate(boolean active) {}
  public record UserView(Long id, String userId, String fullName, String email, String role, Boolean active,
                         Boolean mustChangePassword, java.time.OffsetDateTime lastLoginAt) {
    static UserView from(AppUser user) {
      return new UserView(user.getId(), user.getUserId(), user.getFullName(), user.getEmail(), user.getRole(),
          user.getActive(), user.getMustChangePassword(), user.getLastLoginAt());
    }
  }
}
