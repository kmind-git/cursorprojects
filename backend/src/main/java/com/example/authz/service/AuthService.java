package com.example.authz.service;

import com.example.authz.domain.RefreshTokenEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.domain.UserStatus;
import com.example.authz.repo.RefreshTokenRepository;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserPermissionService userPermissionService;
  private final RefreshTokenService refreshTokenService;

  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      UserPermissionService userPermissionService,
      RefreshTokenService refreshTokenService) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.userPermissionService = userPermissionService;
    this.refreshTokenService = refreshTokenService;
  }

  public static final class LoginResult {
    private final UserEntity user;
    private final String accessToken;
    private final Set<String> permissions;
    private final String refreshToken;

    public LoginResult(UserEntity user, String accessToken, Set<String> permissions, String refreshToken) {
      this.user = user;
      this.accessToken = accessToken;
      this.permissions = permissions;
      this.refreshToken = refreshToken;
    }

    public UserEntity getUser() {
      return user;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public Set<String> getPermissions() {
      return permissions;
    }

    public String getRefreshToken() {
      return refreshToken;
    }
  }

  @Transactional
  public LoginResult login(String username, String password, String device, String ip, String ua) {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("invalid_credentials"));
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new IllegalArgumentException("invalid_credentials");
    }

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("invalid_credentials");
    }

    user.setLastLoginAt(Instant.now());

    Set<String> perms = userPermissionService.getPermissionCodes(user);
    List<String> sortedPerms = new ArrayList<>(perms);
    Collections.sort(sortedPerms);
    String accessToken = jwtService.issueAccessToken(user.getId(), user.getUsername(), sortedPerms);
    String refreshToken = refreshTokenService.issue(user, device, ip, ua).getRawToken();

    return new LoginResult(user, accessToken, perms, refreshToken);
  }

  public static final class RefreshResult {
    private final UserEntity user;
    private final String accessToken;
    private final Set<String> permissions;
    private final String newRefreshToken;

    public RefreshResult(UserEntity user, String accessToken, Set<String> permissions, String newRefreshToken) {
      this.user = user;
      this.accessToken = accessToken;
      this.permissions = permissions;
      this.newRefreshToken = newRefreshToken;
    }

    public UserEntity getUser() {
      return user;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public Set<String> getPermissions() {
      return permissions;
    }

    public String getNewRefreshToken() {
      return newRefreshToken;
    }
  }

  @Transactional
  public RefreshResult refresh(String rawRefreshToken, String device, String ip, String ua) {
    if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
      throw new IllegalArgumentException("missing_refresh_token");
    }

    String hash = refreshTokenService.sha256Hex(rawRefreshToken);
    com.example.authz.domain.RefreshTokenEntity existing =
        refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() -> new IllegalArgumentException("invalid_refresh_token"));

    if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("invalid_refresh_token");
    }

    existing.setRevokedAt(Instant.now());

    UserEntity user = existing.getUser();
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new IllegalArgumentException("invalid_refresh_token");
    }

    Set<String> perms = userPermissionService.getPermissionCodes(user);
    List<String> sortedPerms = new ArrayList<>(perms);
    Collections.sort(sortedPerms);
    String accessToken = jwtService.issueAccessToken(user.getId(), user.getUsername(), sortedPerms);
    String newRefreshToken = refreshTokenService.issue(user, device, ip, ua).getRawToken();

    return new RefreshResult(user, accessToken, perms, newRefreshToken);
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
      log.info("action=auth.session result=LOGOUT_SKIP reason=no_refresh_token");
      return;
    }
    String hash = refreshTokenService.sha256Hex(rawRefreshToken);
    Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(hash);
    if (tokenOpt.isPresent()) {
      RefreshTokenEntity t = tokenOpt.get();
      t.setRevokedAt(Instant.now());
      log.info("action=auth.session userId={} result=LOGOUT_OK", t.getUser().getId());
    } else {
      log.warn("action=auth.session result=LOGOUT_UNKNOWN_TOKEN");
    }
  }
}

