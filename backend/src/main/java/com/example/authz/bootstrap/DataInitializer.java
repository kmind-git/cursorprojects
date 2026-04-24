package com.example.authz.bootstrap;

import com.example.authz.domain.PermissionEntity;
import com.example.authz.domain.RoleEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.domain.UserStatus;
import com.example.authz.domain.MenuEntity;
import com.example.authz.repo.MenuRepository;
import com.example.authz.repo.PermissionRepository;
import com.example.authz.repo.RoleRepository;
import com.example.authz.repo.UserRepository;
import java.util.List;
import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final MenuRepository menuRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PermissionRepository permissionRepository,
      MenuRepository menuRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.menuRepository = menuRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    RoleEntity superAdmin = roleRepository.findByCode("SUPER_ADMIN").orElseGet(() -> {
      RoleEntity r = new RoleEntity();
      r.setCode("SUPER_ADMIN");
      r.setName("Super Admin");
      r.setDescription("System super administrator");
      return roleRepository.save(r);
    });

    List<PermissionSeed> seeds =
        Arrays.asList(
            new PermissionSeed("user:read", "User Read"),
            new PermissionSeed("user:create", "User Create"),
            new PermissionSeed("user:update", "User Update"),
            new PermissionSeed("user:delete", "User Delete"),
            new PermissionSeed("user:assignRole", "Assign Role"),
            new PermissionSeed("role:read", "Role Read"),
            new PermissionSeed("role:create", "Role Create"),
            new PermissionSeed("role:update", "Role Update"),
            new PermissionSeed("role:delete", "Role Delete"),
            new PermissionSeed("role:grant", "Role Grant Permissions"),
            new PermissionSeed("permission:read", "Permission Read"),
            new PermissionSeed("permission:create", "Permission Create"),
            new PermissionSeed("permission:update", "Permission Update"),
            new PermissionSeed("permission:delete", "Permission Delete"),
            new PermissionSeed("menu:read", "Menu Read"),
            new PermissionSeed("menu:create", "Menu Create"),
            new PermissionSeed("menu:update", "Menu Update"),
            new PermissionSeed("menu:delete", "Menu Delete"),
            new PermissionSeed("audit:read", "Audit Log Read"));

    for (PermissionSeed seed : seeds) {
      permissionRepository
          .findByCode(seed.code())
          .orElseGet(() -> {
            PermissionEntity p = new PermissionEntity();
            p.setCode(seed.code());
            p.setName(seed.name());
            p.setDescription(seed.name());
            return permissionRepository.save(p);
          });
    }

    // ensure SUPER_ADMIN has all permissions
    List<PermissionEntity> allPerms = permissionRepository.findAll();
    superAdmin.getPermissions().clear();
    superAdmin.getPermissions().addAll(allPerms);
    roleRepository.save(superAdmin);

    seedMenus();

    if (!userRepository.existsByUsername("admin")) {
      UserEntity u = new UserEntity();
      u.setUsername("admin");
      u.setPasswordHash(passwordEncoder.encode("admin123"));
      u.setStatus(UserStatus.ACTIVE);
      u.getRoles().add(superAdmin);
      userRepository.save(u);
    }

    if (!userRepository.existsByUsername("quoqiang")) {
      UserEntity u = new UserEntity();
      u.setUsername("quoqiang");
      u.setPasswordHash(passwordEncoder.encode("admin123"));
      u.setStatus(UserStatus.ACTIVE);
      u.getRoles().add(superAdmin);
      userRepository.save(u);
    }
  }

  private void seedMenus() {
    if (!menuRepository.findAll().isEmpty()) {
      return;
    }

    PermissionEntity userRead = permissionRepository.findByCode("user:read").get();
    PermissionEntity roleRead = permissionRepository.findByCode("role:read").get();
    PermissionEntity permRead = permissionRepository.findByCode("permission:read").get();
    PermissionEntity menuRead = permissionRepository.findByCode("menu:read").get();
    PermissionEntity auditRead = permissionRepository.findByCode("audit:read").get();

    MenuEntity dashboard = new MenuEntity();
    dashboard.setName("Dashboard");
    dashboard.setPath("/dashboard");
    dashboard.setComponent("Dashboard");
    dashboard.setIcon("Odometer");
    dashboard.setSort(10);
    dashboard.setVisible(true);
    menuRepository.save(dashboard);

    MenuEntity system = new MenuEntity();
    system.setName("System");
    system.setPath("/system");
    system.setComponent("Layout");
    system.setIcon("Setting");
    system.setSort(20);
    system.setVisible(true);
    menuRepository.save(system);

    menuRepository.save(menuChild(system, "Users", "/system/users", "SystemUsers", "User", 1, userRead));
    menuRepository.save(menuChild(system, "Roles", "/system/roles", "SystemRoles", "Avatar", 2, roleRead));
    menuRepository.save(menuChild(system, "Permissions", "/system/permissions", "SystemPermissions", "Key", 3, permRead));
    menuRepository.save(menuChild(system, "Menus", "/system/menus", "SystemMenus", "Menu", 4, menuRead));
    menuRepository.save(menuChild(system, "AuditLogs", "/system/audit-logs", "SystemAuditLogs", "Document", 5, auditRead));
  }

  private MenuEntity menuChild(
      MenuEntity parent,
      String name,
      String path,
      String component,
      String icon,
      int sort,
      PermissionEntity requiredPermission) {
    MenuEntity m = new MenuEntity();
    m.setParent(parent);
    m.setName(name);
    m.setPath(path);
    m.setComponent(component);
    m.setIcon(icon);
    m.setSort(sort);
    m.setVisible(true);
    m.getPermissions().add(requiredPermission);
    return m;
  }

  private static final class PermissionSeed {
    private final String code;
    private final String name;

    private PermissionSeed(String code, String name) {
      this.code = code;
      this.name = name;
    }

    public String code() {
      return code;
    }

    public String name() {
      return name;
    }
  }
}

