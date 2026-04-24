package com.example.authz.service;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class CsrfTokenService {
  private final SecureRandom secureRandom = new SecureRandom();

  public String newToken() {
    byte[] bytes = new byte[24];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

