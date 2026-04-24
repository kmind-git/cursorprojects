package com.example.authz.domain;

import java.time.Instant;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_refresh_token")
public class RefreshTokenEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "token_hash", nullable = false, unique = true, length = 200)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(length = 200)
  private String device;

  @Column(length = 64)
  private String ip;

  @Column(length = 500)
  private String ua;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }
}

