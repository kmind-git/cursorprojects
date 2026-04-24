package com.example.authz.service;

import com.example.authz.config.AppSecurityProperties;
import com.example.authz.domain.RefreshTokenEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.repo.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
  private final AppSecurityProperties props;
  private final RefreshTokenRepository refreshTokenRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenService(AppSecurityProperties props, RefreshTokenRepository refreshTokenRepository) {
    this.props = props;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public static final class IssuedRefreshToken {
    private final String rawToken;
    private final RefreshTokenEntity entity;

    public IssuedRefreshToken(String rawToken, RefreshTokenEntity entity) {
      this.rawToken = rawToken;
      this.entity = entity;
    }

    public String getRawToken() {
      return rawToken;
    }

    public RefreshTokenEntity getEntity() {
      return entity;
    }
  }

  @Transactional
  public IssuedRefreshToken issue(UserEntity user, String device, String ip, String ua) {
    String raw = generateOpaqueToken();
    String hash = sha256Hex(raw);

    Instant now = Instant.now();
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setUser(user);
    entity.setTokenHash(hash);
    entity.setExpiresAt(now.plus(props.getRefresh().getTtlDays(), ChronoUnit.DAYS));
    entity.setDevice(device);
    entity.setIp(ip);
    entity.setUa(ua);

    refreshTokenRepository.save(entity);
    return new IssuedRefreshToken(raw, entity);
  }

  public String sha256Hex(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String generateOpaqueToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

