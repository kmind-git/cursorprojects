package com.example.authz.api.admin;

import com.example.authz.domain.RoleEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.domain.UserStatus;
import com.example.authz.repo.RoleRepository;
import com.example.authz.repo.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserAdminController {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserAdminController(
      UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public static final class UserDto {
    public Long id;
    public String username;
    public UserStatus status;
    public Set<String> roles;

    public static UserDto from(UserEntity u) {
      UserDto dto = new UserDto();
      dto.id = u.getId();
      dto.username = u.getUsername();
      dto.status = u.getStatus();
      dto.roles = u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet());
      return dto;
    }
  }

  public static final class CreateUserRequest {
    @NotBlank public String username;
    @NotBlank public String password;
  }

  public static final class UpdateUserStatusRequest {
    @NotNull public UserStatus status;
  }

  public static final class ResetPasswordRequest {
    @NotBlank public String newPassword;
  }

  public static final class AssignRolesRequest {
    @NotNull public List<String> roleCodes;
  }

  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping
  public List<UserDto> list() {
    List<UserDto> out = new ArrayList<>();
    for (UserEntity u : userRepository.findAll()) {
      out.add(UserDto.from(u));
    }
    return out;
  }

  @PreAuthorize("hasAuthority('user:create')")
  @PostMapping
  @Transactional
  public UserDto create(@Valid @RequestBody CreateUserRequest req) {
    if (userRepository.existsByUsername(req.username)) {
      throw new IllegalArgumentException("username_taken");
    }
    UserEntity u = new UserEntity();
    u.setUsername(req.username);
    u.setPasswordHash(passwordEncoder.encode(req.password));
    u.setStatus(UserStatus.ACTIVE);
    return UserDto.from(userRepository.save(u));
  }

  @PreAuthorize("hasAuthority('user:update')")
  @PatchMapping("/{id}/status")
  @Transactional
  public UserDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest req) {
    UserEntity u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("user_not_found"));
    u.setStatus(req.status);
    return UserDto.from(u);
  }

  @PreAuthorize("hasAuthority('user:update')")
  @PostMapping("/{id}/reset-password")
  @Transactional
  public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest req) {
    UserEntity u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("user_not_found"));
    u.setPasswordHash(passwordEncoder.encode(req.newPassword));
  }

  @PreAuthorize("hasAuthority('user:delete')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    userRepository.deleteById(id);
  }

  @PreAuthorize("hasAuthority('user:assignRole')")
  @PostMapping("/{id}/roles")
  @Transactional
  public UserDto assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest req) {
    UserEntity u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("user_not_found"));
    u.getRoles().clear();
    for (String code : req.roleCodes) {
      Optional<RoleEntity> role = roleRepository.findByCode(code);
      if (!role.isPresent()) {
        throw new IllegalArgumentException("role_not_found:" + code);
      }
      u.getRoles().add(role.get());
    }
    return UserDto.from(u);
  }
}

