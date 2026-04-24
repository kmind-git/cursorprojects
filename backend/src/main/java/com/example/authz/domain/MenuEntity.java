package com.example.authz.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_menu")
public class MenuEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private MenuEntity parent;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 200)
  private String path;

  @Column(length = 200)
  private String component;

  @Column(length = 64)
  private String icon;

  @Column(nullable = false)
  private int sort;

  @Column(nullable = false)
  private boolean visible;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "sys_menu_permission",
      joinColumns = @JoinColumn(name = "menu_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<PermissionEntity> permissions = new HashSet<>();

  @PrePersist
  public void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = Instant.now();
  }
}

