package com.example.authz.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {
  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping("/admin/ping")
  public String adminPing() {
    return "admin-pong";
  }
}

