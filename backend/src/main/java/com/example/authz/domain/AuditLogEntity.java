package com.example.authz.domain;

import java.time.Instant;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_audit_log")
public class AuditLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_user_id")
  private UserEntity actorUser;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(name = "target_type", length = 64)
  private String targetType;

  @Column(name = "target_id", length = 64)
  private String targetId;

  @Column(nullable = false, length = 16)
  private String result;

  @Column(length = 2000)
  private String detail;

  @Column(length = 64)
  private String ip;

  @Column(length = 500)
  private String ua;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
  }
}

