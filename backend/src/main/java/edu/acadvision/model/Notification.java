package edu.acadvision.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, length = 60)
  private String userId;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @Column(name = "notification_type", nullable = false, length = 40)
  @Builder.Default
  private String notificationType = "GENERAL";

  @Column(name = "link_url", length = 255)
  private String linkUrl;

  @Column(nullable = false)
  @Builder.Default
  private Boolean read = false;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;
}
