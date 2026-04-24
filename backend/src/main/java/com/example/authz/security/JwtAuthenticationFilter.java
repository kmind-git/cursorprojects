package com.example.authz.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring("Bearer ".length()).trim();
      try {
        JwtService.JwtPrincipal principal = jwtService.parseAccessToken(token);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                principal, null, principal.authorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (Exception ignored) {
        // Invalid token -> treat as anonymous; entrypoint will handle when needed
      }
    }

    filterChain.doFilter(request, response);
  }
}

