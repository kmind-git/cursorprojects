package com.example.authz.repo;

import com.example.authz.domain.RefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  @Transactional
  @Modifying
  @Query("delete from RefreshTokenEntity t where t.expiresAt < :now or t.revokedAt is not null")
  int deleteExpiredOrRevoked(Instant now);
}

