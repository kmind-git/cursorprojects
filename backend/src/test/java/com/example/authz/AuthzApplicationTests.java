package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.authz.domain.UserEntity;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthzApplicationTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void contextLoads() {}

  @Test
  void loginFailsWithBadPassword() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("username", "admin");
    body.put("password", "wrong-password");
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  /** 登录接口成功：需测试库中存在 DataInitializer 写入的 admin / admin123。 */
  @Test
  void loginSucceedsWithSeededAdmin() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("username", "admin");
    body.put("password", "admin123");
    MvcResult res =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
    assertNotNull(json.get("accessToken"), "响应应包含 accessToken");
    assertTrue(json.get("accessToken").asText().length() > 10);
    assertEquals("admin", json.get("user").get("username").asText());
    assertTrue(json.has("permissions"));
    assertTrue(json.has("menus"));
  }

  /**
   * Creates user "123" via POST /api/users using a JWT with {@code user:create}. Requires seeded
   * {@code admin} (DataInitializer). Second run may get 400 {@code username_taken}.
   */
  @Test
  void createUserNamed123() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("Expected seeded user 'admin' in test DB"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:create"));

    Map<String, String> create = new HashMap<>();
    create.put("username", "123");
    create.put("password", "TempPass123");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(create)))
            .andReturn();

    int sc = result.getResponse().getStatus();
    String body = result.getResponse().getContentAsString();
    assertTrue(
        sc == 200 || (sc == 400 && body.contains("username_taken")),
        "Unexpected status=" + sc + " body=" + body);
  }
}
