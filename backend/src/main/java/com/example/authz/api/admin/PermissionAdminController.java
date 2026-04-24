package com.example.authz.api.admin;

import com.example.authz.domain.PermissionEntity;
import com.example.authz.repo.PermissionRepository;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionAdminController {
  private final PermissionRepository permissionRepository;

  public PermissionAdminController(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public static final class PermissionDto {
    public Long id;
    public String code;
    public String name;
    public String description;

    public static PermissionDto from(PermissionEntity p) {
      PermissionDto dto = new PermissionDto();
      dto.id = p.getId();
      dto.code = p.getCode();
      dto.name = p.getName();
      dto.description = p.getDescription();
      return dto;
    }
  }

  public static final class UpsertPermissionRequest {
    @NotBlank public String code;
    @NotBlank public String name;
    public String description;
  }

  @PreAuthorize("hasAuthority('permission:read')")
  @GetMapping
  public List<PermissionDto> list() {
    List<PermissionDto> out = new ArrayList<>();
    for (PermissionEntity p : permissionRepository.findAll()) {
      out.add(PermissionDto.from(p));
    }
    return out;
  }

  @PreAuthorize("hasAuthority('permission:create')")
  @PostMapping
  @Transactional
  public PermissionDto create(@Valid @RequestBody UpsertPermissionRequest req) {
    if (permissionRepository.findByCode(req.code).isPresent()) {
      throw new IllegalArgumentException("permission_code_taken");
    }
    PermissionEntity p = new PermissionEntity();
    p.setCode(req.code);
    p.setName(req.name);
    p.setDescription(req.description);
    return PermissionDto.from(permissionRepository.save(p));
  }

  @PreAuthorize("hasAuthority('permission:update')")
  @PutMapping("/{id}")
  @Transactional
  public PermissionDto update(@PathVariable Long id, @Valid @RequestBody UpsertPermissionRequest req) {
    PermissionEntity p = permissionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("permission_not_found"));
    p.setCode(req.code);
    p.setName(req.name);
    p.setDescription(req.description);
    return PermissionDto.from(p);
  }

  @PreAuthorize("hasAuthority('permission:delete')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    permissionRepository.deleteById(id);
  }
}

