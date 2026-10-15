package edu.acadvision.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true, length = 60)
  @NotBlank
  private String userId;

  @Column(name = "full_name", nullable = false, length = 160)
  @NotBlank
  private String fullName;

  @Column(nullable = false, unique = true, length = 180)
  @Email
  @NotBlank
  private String email;

  @Column(name = "password_hash", nullable = false, length = 120)
  @NotBlank
  private String passwordHash;

  @Column(nullable = false, length = 20)
  @NotBlank
  private String role; // ADMIN, FACULTY, STUDENT

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @Column(name = "must_change_password", nullable = false)
  @Builder.Default
  private Boolean mustChangePassword = false;

  @Column(name = "mfa_enabled", nullable = false)
  @Builder.Default
  private Boolean mfaEnabled = false;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private OffsetDateTime updatedAt;
}
