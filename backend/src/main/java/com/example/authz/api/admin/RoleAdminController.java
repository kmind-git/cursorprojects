package com.example.authz.api.admin;

import com.example.authz.domain.PermissionEntity;
import com.example.authz.domain.RoleEntity;
import com.example.authz.repo.PermissionRepository;
import com.example.authz.repo.RoleRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleAdminController {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public RoleAdminController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  public static final class RoleDto {
    public Long id;
    public String code;
    public String name;
    public String description;
    public Set<String> permissions;

    public static RoleDto from(RoleEntity r) {
      RoleDto dto = new RoleDto();
      dto.id = r.getId();
      dto.code = r.getCode();
      dto.name = r.getName();
      dto.description = r.getDescription();
      dto.permissions =
          r.getPermissions().stream().map(PermissionEntity::getCode).collect(Collectors.toSet());
      return dto;
    }
  }

  public static final class UpsertRoleRequest {
    @NotBlank public String code;
    @NotBlank public String name;
    public String description;
  }

  public static final class GrantPermissionsRequest {
    @NotNull public List<String> permissionCodes;
  }

  @PreAuthorize("hasAuthority('role:read')")
  @GetMapping
  public List<RoleDto> list() {
    List<RoleDto> out = new ArrayList<>();
    for (RoleEntity r : roleRepository.findAll()) {
      out.add(RoleDto.from(r));
    }
    return out;
  }

  @PreAuthorize("hasAuthority('role:create')")
  @PostMapping
  @Transactional
  public RoleDto create(@Valid @RequestBody UpsertRoleRequest req) {
    if (roleRepository.findByCode(req.code).isPresent()) {
      throw new IllegalArgumentException("role_code_taken");
    }
    RoleEntity r = new RoleEntity();
    r.setCode(req.code);
    r.setName(req.name);
    r.setDescription(req.description);
    return RoleDto.from(roleRepository.save(r));
  }

  @PreAuthorize("hasAuthority('role:update')")
  @PutMapping("/{id}")
  @Transactional
  public RoleDto update(@PathVariable Long id, @Valid @RequestBody UpsertRoleRequest req) {
    RoleEntity r = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("role_not_found"));
    r.setCode(req.code);
    r.setName(req.name);
    r.setDescription(req.description);
    return RoleDto.from(r);
  }

  @PreAuthorize("hasAuthority('role:delete')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    roleRepository.deleteById(id);
  }

  @PreAuthorize("hasAuthority('role:grant')")
  @PostMapping("/{id}/permissions")
  @Transactional
  public RoleDto grant(@PathVariable Long id, @Valid @RequestBody GrantPermissionsRequest req) {
    RoleEntity r = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("role_not_found"));
    Set<PermissionEntity> perms = new HashSet<>();
    for (String code : req.permissionCodes) {
      Optional<PermissionEntity> p = permissionRepository.findByCode(code);
      if (!p.isPresent()) {
        throw new IllegalArgumentException("permission_not_found:" + code);
      }
      perms.add(p.get());
    }
    r.getPermissions().clear();
    r.getPermissions().addAll(perms);
    return RoleDto.from(r);
  }
}

