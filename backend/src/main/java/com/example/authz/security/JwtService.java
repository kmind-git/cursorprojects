package com.example.authz.security;

import com.example.authz.config.AppSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final AppSecurityProperties props;

  public JwtService(AppSecurityProperties props) {
    this.props = props;
  }

  public String issueAccessToken(long userId, String username, List<String> permissionCodes) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.getJwt().getAccessTtlSeconds());
    byte[] keyBytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);

    return Jwts.builder()
        .setIssuer(props.getJwt().getIssuer())
        .setSubject(Long.toString(userId))
        .claim("username", username)
        .claim("perms", permissionCodes)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
        .compact();
  }

  public JwtPrincipal parseAccessToken(String jwt) {
    byte[] keyBytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    Claims claims =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
            .build()
            .parseClaimsJws(jwt)
            .getBody();

    long userId = Long.parseLong(claims.getSubject());
    String username = claims.get("username", String.class);
    @SuppressWarnings("unchecked")
    List<String> perms = (List<String>) claims.get("perms", List.class);
    if (perms == null) {
      perms = Collections.emptyList();
    }

    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String p : perms) {
      authorities.add(new SimpleGrantedAuthority(p));
    }
    return new JwtPrincipal(userId, username, authorities);
  }

  public static final class JwtPrincipal {
    private final long userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtPrincipal(
        long userId, String username, Collection<? extends GrantedAuthority> authorities) {
      this.userId = userId;
      this.username = username;
      this.authorities = authorities;
    }

    public long userId() {
      return userId;
    }

    public String username() {
      return username;
    }

    public Collection<? extends GrantedAuthority> authorities() {
      return authorities;
    }
  }
}

