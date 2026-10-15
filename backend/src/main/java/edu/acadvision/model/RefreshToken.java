package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, length = 60)
  private String userId;

  @Column(nullable = false, unique = true, length = 255)
  private String token;

  @Column(name = "expiry_date", nullable = false)
  private OffsetDateTime expiryDate;

  @Column(nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
