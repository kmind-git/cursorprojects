package com.example.authz.repo;

import com.example.authz.domain.MenuEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
  List<MenuEntity> findAllByOrderBySortAscIdAsc();
}

