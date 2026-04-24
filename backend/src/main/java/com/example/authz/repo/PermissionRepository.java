package com.example.authz.repo;

import com.example.authz.domain.PermissionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
  Optional<PermissionEntity> findByCode(String code);
}

