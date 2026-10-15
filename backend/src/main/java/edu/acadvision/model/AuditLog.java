package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "actor_user_id", nullable = false, length = 60)
  private String actorUserId;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 80)
  private String entityType;

  @Column(name = "entity_id", length = 100)
  private String entityId;

  @Column(columnDefinition = "TEXT")
  private String details;

  @Column(name = "impersonation_session", nullable = false)
  @Builder.Default
  private Boolean impersonationSession = false;

  @Column(name = "impersonation_reason")
  private String impersonationReason;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
