package com.example.authz.api.admin;

import com.example.authz.domain.MenuEntity;
import com.example.authz.domain.PermissionEntity;
import com.example.authz.repo.MenuRepository;
import com.example.authz.repo.PermissionRepository;
import com.example.authz.service.MenuService;
import java.util.ArrayList;
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
@RequestMapping("/api/menus")
public class MenuAdminController {
  private final MenuRepository menuRepository;
  private final PermissionRepository permissionRepository;
  private final MenuService menuService;

  public MenuAdminController(
      MenuRepository menuRepository, PermissionRepository permissionRepository, MenuService menuService) {
    this.menuRepository = menuRepository;
    this.permissionRepository = permissionRepository;
    this.menuService = menuService;
  }

  public static final class MenuDto {
    public Long id;
    public Long parentId;
    public String name;
    public String path;
    public String component;
    public String icon;
    public int sort;
    public boolean visible;
    public Set<String> permissions;

    public static MenuDto from(MenuEntity m) {
      MenuDto dto = new MenuDto();
      dto.id = m.getId();
      dto.parentId = m.getParent() == null ? null : m.getParent().getId();
      dto.name = m.getName();
      dto.path = m.getPath();
      dto.component = m.getComponent();
      dto.icon = m.getIcon();
      dto.sort = m.getSort();
      dto.visible = m.isVisible();
      dto.permissions =
          m.getPermissions().stream().map(PermissionEntity::getCode).collect(Collectors.toSet());
      return dto;
    }
  }

  public static final class UpsertMenuRequest {
    public Long parentId;
    @NotBlank public String name;
    @NotBlank public String path;
    public String component;
    public String icon;
    public int sort;
    public boolean visible = true;
    @NotNull public List<String> permissionCodes;
  }

  @PreAuthorize("hasAuthority('menu:read')")
  @GetMapping
  public List<MenuDto> list() {
    List<MenuDto> out = new ArrayList<>();
    for (MenuEntity m : menuRepository.findAllByOrderBySortAscIdAsc()) {
      out.add(MenuDto.from(m));
    }
    return out;
  }

  @PreAuthorize("hasAuthority('menu:read')")
  @GetMapping("/tree")
  public List<MenuService.MenuNode> tree() {
    // management tree is full tree (no filtering), reuse service by passing all permission codes
    Set<String> allPerms =
        permissionRepository.findAll().stream().map(PermissionEntity::getCode).collect(Collectors.toSet());
    return menuService.getMenuTreeForPermissions(allPerms);
  }

  @PreAuthorize("hasAuthority('menu:create')")
  @PostMapping
  @Transactional
  public MenuDto create(@Valid @RequestBody UpsertMenuRequest req) {
    MenuEntity m = new MenuEntity();
    apply(req, m);
    return MenuDto.from(menuRepository.save(m));
  }

  @PreAuthorize("hasAuthority('menu:update')")
  @PutMapping("/{id}")
  @Transactional
  public MenuDto update(@PathVariable Long id, @Valid @RequestBody UpsertMenuRequest req) {
    MenuEntity m = menuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("menu_not_found"));
    apply(req, m);
    return MenuDto.from(m);
  }

  @PreAuthorize("hasAuthority('menu:delete')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    menuRepository.deleteById(id);
  }

  private void apply(UpsertMenuRequest req, MenuEntity m) {
    if (req.parentId == null) {
      m.setParent(null);
    } else {
      MenuEntity parent =
          menuRepository.findById(req.parentId).orElseThrow(() -> new IllegalArgumentException("menu_parent_not_found"));
      m.setParent(parent);
    }
    m.setName(req.name);
    m.setPath(req.path);
    m.setComponent(req.component);
    m.setIcon(req.icon);
    m.setSort(req.sort);
    m.setVisible(req.visible);
    m.getPermissions().clear();
    for (String code : req.permissionCodes) {
      Optional<PermissionEntity> p = permissionRepository.findByCode(code);
      if (!p.isPresent()) {
        throw new IllegalArgumentException("permission_not_found:" + code);
      }
      m.getPermissions().add(p.get());
    }
  }
}

