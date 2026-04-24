package com.example.authz.api;

import com.example.authz.config.AppSecurityProperties;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import com.example.authz.service.AuditService;
import com.example.authz.service.AuthService;
import com.example.authz.service.CsrfTokenService;
import com.example.authz.service.MenuService;
import com.example.authz.service.UserPermissionService;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final AppSecurityProperties props;
  private final AuthService authService;
  private final CsrfTokenService csrfTokenService;
  private final UserRepository userRepository;
  private final UserPermissionService userPermissionService;
  private final MenuService menuService;
  private final AuditService auditService;

  public AuthController(
      AppSecurityProperties props,
      AuthService authService,
      CsrfTokenService csrfTokenService,
      UserRepository userRepository,
      UserPermissionService userPermissionService,
      MenuService menuService,
      AuditService auditService) {
    this.props = props;
    this.authService = authService;
    this.csrfTokenService = csrfTokenService;
    this.userRepository = userRepository;
    this.userPermissionService = userPermissionService;
    this.menuService = menuService;
    this.auditService = auditService;
  }

  public static final class LoginRequest {
    @NotBlank public String username;
    @NotBlank public String password;
  }

  public static final class AuthResponse {
    public String accessToken;
    public long expiresInSeconds;
    public UserProfile user;
    public Set<String> permissions;
    public List<MenuService.MenuNode> menus;

    public AuthResponse(
        String accessToken,
        long expiresInSeconds,
        UserProfile user,
        Set<String> permissions,
        List<MenuService.MenuNode> menus) {
      this.accessToken = accessToken;
      this.expiresInSeconds = expiresInSeconds;
      this.user = user;
      this.permissions = permissions;
      this.menus = menus;
    }
  }

  public static final class UserProfile {
    public long id;
    public String username;

    public UserProfile(long id, String username) {
      this.id = id;
      this.username = username;
    }
  }

  @PostMapping("/login")
  public AuthResponse login(
      @Valid @RequestBody LoginRequest req,
      HttpServletRequest request,
      HttpServletResponse response) {
    try {
      AuthService.LoginResult result =
          authService.login(
              req.username,
              req.password,
              request.getHeader("X-Device"),
              request.getRemoteAddr(),
              request.getHeader("User-Agent"));

      auditService.log(
          result.getUser(),
          "auth.login",
          "user",
          String.valueOf(result.getUser().getId()),
          "SUCCESS",
          null,
          request.getRemoteAddr(),
          request.getHeader("User-Agent"));

      setRefreshCookie(response, result.getRefreshToken());
      setCsrfCookie(response, csrfTokenService.newToken());

      List<MenuService.MenuNode> menus =
          menuService.getMenuTreeForPermissions(result.getPermissions());
      log.info(
          "action=auth.login userId={} username={} result=SUCCESS",
          result.getUser().getId(),
          result.getUser().getUsername());
      return new AuthResponse(
          result.getAccessToken(),
          props.getJwt().getAccessTtlSeconds(),
          new UserProfile(result.getUser().getId(), result.getUser().getUsername()),
          result.getPermissions(),
          menus);
    } catch (IllegalArgumentException e) {
      log.warn(
          "action=auth.login username={} result=FAILURE reason={}",
          req.username,
          e.getMessage());
      auditService.log(
          null,
          "auth.login",
          "user",
          req.username,
          "FAILURE",
          e.getMessage(),
          request.getRemoteAddr(),
          request.getHeader("User-Agent"));
      throw e;
    }
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
    requireCsrf(request);
    String rawRefresh = readCookie(request, props.getRefresh().getCookieName());

    try {
      AuthService.RefreshResult result =
          authService.refresh(
              rawRefresh,
              request.getHeader("X-Device"),
              request.getRemoteAddr(),
              request.getHeader("User-Agent"));

      setRefreshCookie(response, result.getNewRefreshToken());
      setCsrfCookie(response, csrfTokenService.newToken());

      List<MenuService.MenuNode> menus =
          menuService.getMenuTreeForPermissions(result.getPermissions());
      log.info(
          "action=auth.refresh userId={} username={} result=SUCCESS",
          result.getUser().getId(),
          result.getUser().getUsername());
      return new AuthResponse(
          result.getAccessToken(),
          props.getJwt().getAccessTtlSeconds(),
          new UserProfile(result.getUser().getId(), result.getUser().getUsername()),
          result.getPermissions(),
          menus);
    } catch (IllegalArgumentException e) {
      log.warn("action=auth.refresh result=FAILURE reason={}", e.getMessage());
      throw e;
    }
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    requireCsrf(request);
    String rawRefresh = readCookie(request, props.getRefresh().getCookieName());
    authService.logout(rawRefresh);
    log.info("action=auth.logout result=ACCEPTED");
    clearCookie(response, props.getRefresh().getCookieName(), true, "/api/auth");
    clearCookie(response, props.getCsrf().getCookieName(), false, "/");
  }

  @GetMapping("/me")
  public AuthResponse me(@AuthenticationPrincipal JwtService.JwtPrincipal principal) {
    com.example.authz.domain.UserEntity user =
        userRepository
            .findById(principal.userId())
            .orElseThrow(() -> new IllegalStateException("user_not_found"));
    Set<String> perms = userPermissionService.getPermissionCodes(user);
    List<MenuService.MenuNode> menus = menuService.getMenuTreeForPermissions(perms);
    log.info("action=auth.me userId={} username={}", user.getId(), user.getUsername());
    return new AuthResponse(
        null,
        props.getJwt().getAccessTtlSeconds(),
        new UserProfile(user.getId(), user.getUsername()),
        perms,
        menus);
  }

  private void requireCsrf(HttpServletRequest request) {
    String cookieToken = readCookie(request, props.getCsrf().getCookieName());
    String headerToken = request.getHeader(props.getCsrf().getHeaderName());
    if (cookieToken == null || headerToken == null || !cookieToken.equals(headerToken)) {
      throw new IllegalArgumentException("csrf_failed");
    }
  }

  private String readCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    for (Cookie c : cookies) {
      if (name.equals(c.getName())) return c.getValue();
    }
    return null;
  }

  private void setRefreshCookie(HttpServletResponse response, String rawToken) {
    ResponseCookie cookie =
        ResponseCookie.from(props.getRefresh().getCookieName(), rawToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(Duration.ofDays(props.getRefresh().getTtlDays()))
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void setCsrfCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie =
        ResponseCookie.from(props.getCsrf().getCookieName(), token)
            .httpOnly(false)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(props.getRefresh().getTtlDays()))
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void clearCookie(HttpServletResponse response, String name, boolean httpOnly, String path) {
    ResponseCookie cookie =
        ResponseCookie.from(name, "")
            .httpOnly(httpOnly)
            .secure(false)
            .sameSite("Lax")
            .path(path)
            .maxAge(Duration.ZERO)
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }
}

