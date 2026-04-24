package com.example.authz.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
  private Cors cors = new Cors();
  private Jwt jwt = new Jwt();
  private Refresh refresh = new Refresh();
  private Csrf csrf = new Csrf();

  @Data
  public static class Cors {
    private List<String> allowedOrigins;
  }

  @Data
  public static class Jwt {
    private String issuer = "authz";
    private long accessTtlSeconds = 900;
    private String secret;
  }

  @Data
  public static class Refresh {
    private int ttlDays = 14;
    private String cookieName = "refresh_token";
  }

  @Data
  public static class Csrf {
    private String cookieName = "csrf_token";
    private String headerName = "X-CSRF-Token";
  }
}

