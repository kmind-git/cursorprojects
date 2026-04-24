package com.example.authz.security;

import com.example.authz.config.AppSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
  private final AppSecurityProperties props;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(
      AppSecurityProperties props,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      ObjectMapper objectMapper) {
    this.props = props;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(eh -> eh
            .authenticationEntryPoint((request, response, authException) -> {
              writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            }))
        .authorizeRequests(auth -> auth
            .antMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(props.getCors().getAllowedOrigins());
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", props.getCsrf().getHeaderName()));
    config.setExposedHeaders(Arrays.asList(props.getCsrf().getHeaderName()));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  private void writeJsonError(HttpServletResponse response, int status, String code) {
    try {
      response.setStatus(status);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(code));
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static final class ErrorResponse {
    public final String code;

    private ErrorResponse(String code) {
      this.code = code;
    }
  }
}

