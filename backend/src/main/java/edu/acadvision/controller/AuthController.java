package edu.acadvision.controller;

import edu.acadvision.dto.*;
import edu.acadvision.model.AppUser;
import edu.acadvision.model.AuditLog;
import edu.acadvision.model.PasswordResetToken;
import edu.acadvision.model.RefreshToken;
import edu.acadvision.repository.AppUserRepository;
import edu.acadvision.repository.AuditLogRepository;
import edu.acadvision.repository.PasswordResetTokenRepository;
import edu.acadvision.repository.RefreshTokenRepository;
import edu.acadvision.security.jwt.JwtService;
import edu.acadvision.security.user.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AppUserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthController(
      AppUserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      AuditLogRepository auditLogRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.auditLogRepository = auditLogRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    AppUser user = userRepository.findByUserIdOrEmail(request.getUsername(), request.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

    if (Boolean.FALSE.equals(user.getActive())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account has been deactivated. Please contact an administrator.");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // Update login timestamp
    user.setLastLoginAt(OffsetDateTime.now());
    userRepository.save(user);

    UserPrincipal principal = new UserPrincipal(user);
    String accessToken = jwtService.generateToken(principal, Map.of("role", user.getRole(), "name", user.getFullName()));
    String refreshToken = UUID.randomUUID().toString();

    // Persist refresh token
    RefreshToken tokenEntity = RefreshToken.builder()
        .userId(user.getUserId())
        .token(refreshToken)
        .expiryDate(OffsetDateTime.now().plusDays(7))
        .revoked(false)
        .build();
    refreshTokenRepository.save(tokenEntity);

    // Record audit log
    auditLogRepository.save(AuditLog.builder()
        .actorUserId(user.getUserId())
        .action("USER_LOGIN")
        .entityType("APP_USER")
        .entityId(user.getUserId())
        .details("Successful authentication for role " + user.getRole())
        .ipAddress(httpRequest.getRemoteAddr())
        .build());

    UserSummaryDto userSummary = UserSummaryDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .role(user.getRole())
        .active(user.getActive())
        .mustChangePassword(user.getMustChangePassword())
        .build();

    return ResponseEntity.ok(AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtService.getExpirationMs() / 1000)
        .user(userSummary)
        .build());
  }

  @PostMapping("/refresh")
  @Transactional
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

    if (Boolean.TRUE.equals(token.getRevoked()) || token.getExpiryDate().isBefore(OffsetDateTime.now())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
    }

    AppUser user = userRepository.findByUserId(token.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (Boolean.FALSE.equals(user.getActive())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
    }

    UserPrincipal principal = new UserPrincipal(user);
    String newAccessToken = jwtService.generateToken(principal, Map.of("role", user.getRole(), "name", user.getFullName()));
    String rotatedRefreshToken = UUID.randomUUID().toString();
    token.setRevoked(true);
    refreshTokenRepository.save(token);
    refreshTokenRepository.save(RefreshToken.builder()
        .userId(user.getUserId())
        .token(rotatedRefreshToken)
        .expiryDate(OffsetDateTime.now().plusDays(7))
        .revoked(false)
        .build());

    UserSummaryDto userSummary = UserSummaryDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .role(user.getRole())
        .active(user.getActive())
        .mustChangePassword(user.getMustChangePassword())
        .build();

    return ResponseEntity.ok(AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(rotatedRefreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtService.getExpirationMs() / 1000)
        .user(userSummary)
        .build());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestBody(required = false) RefreshTokenRequest request) {
    if (request != null && request.getRefreshToken() != null) {
      refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
      });
    }
  }

  @GetMapping("/me")
  public ResponseEntity<UserSummaryDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    AppUser user = userRepository.findByUserId(principal.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    return ResponseEntity.ok(UserSummaryDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .role(user.getRole())
        .active(user.getActive())
        .mustChangePassword(user.getMustChangePassword())
        .build());
  }

  @PutMapping("/change-password")
  @Transactional
  public ResponseEntity<Map<String, String>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    AppUser user = userRepository.findByUserId(principal.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    user.setMustChangePassword(false);
    userRepository.save(user);
    refreshTokenRepository.findAll().stream()
        .filter(token -> token.getUserId().equals(user.getUserId()) && !Boolean.TRUE.equals(token.getRevoked()))
        .forEach(token -> { token.setRevoked(true); refreshTokenRepository.save(token); });
    auditLogRepository.save(AuditLog.builder().actorUserId(user.getUserId()).action("PASSWORD_CHANGED")
        .entityType("APP_USER").entityId(user.getUserId()).details("Password changed by account owner").build());
    return ResponseEntity.ok(Map.of("message", "Password changed. Please sign in again."));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    // Do not reveal whether an account exists or return a reset credential to the caller.
    userRepository.findByUserIdOrEmail(request.getIdentifier(), request.getIdentifier()).ifPresent(user ->
        auditLogRepository.save(AuditLog.builder()
            .actorUserId(user.getUserId())
            .action("FORGOT_PASSWORD_REQUEST")
            .entityType("APP_USER")
            .entityId(user.getUserId())
            .details("Password reset requested; delivery must be configured")
            .build()));
    return ResponseEntity.accepted().body(Map.of(
        "message", "If an eligible account exists, password reset instructions will be sent to its registered contact."
    ));
  }

  @PostMapping("/reset-password")
  @Transactional
  public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

    if (Boolean.TRUE.equals(token.getUsed()) || token.getExpiryDate().isBefore(OffsetDateTime.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired or has already been used");
    }

    AppUser user = userRepository.findByUserId(token.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    user.setMustChangePassword(false);
    userRepository.save(user);

    // A password change invalidates every outstanding browser session.
    refreshTokenRepository.findAll().stream()
        .filter(refresh -> refresh.getUserId().equals(user.getUserId()) && !Boolean.TRUE.equals(refresh.getRevoked()))
        .forEach(refresh -> { refresh.setRevoked(true); refreshTokenRepository.save(refresh); });

    token.setUsed(true);
    passwordResetTokenRepository.save(token);

    auditLogRepository.save(AuditLog.builder()
        .actorUserId(user.getUserId())
        .action("PASSWORD_RESET_SUCCESS")
        .entityType("APP_USER")
        .entityId(user.getUserId())
        .details("Password updated via valid reset token")
        .build());

    return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in with your new credentials."));
  }
}
