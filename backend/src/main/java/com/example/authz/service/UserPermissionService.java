package com.example.authz.service;

import com.example.authz.domain.UserEntity;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionService {
  public Set<String> getPermissionCodes(UserEntity user) {
    Set<String> out = new HashSet<>();
    user.getRoles().forEach(r -> r.getPermissions().forEach(p -> out.add(p.getCode())));
    return out;
  }
}

