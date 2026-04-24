package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.authz.domain.UserEntity;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** 按 {@link UserCreateApiTest} 方式新建用户 guoqiang / admin123。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuoqiangUserCreateApiTest {

  private static final String USERNAME = "guoqiang";
  private static final String PASSWORD = "admin123";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void createUserGuoqiang() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:create"));

    String reqBody = "{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}";

    MvcResult res =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqBody))
            .andReturn();

    int sc = res.getResponse().getStatus();
    String raw = res.getResponse().getContentAsString();

    if (sc == 400 && raw.contains("username_taken")) {
      assertTrue(
          userRepository.findByUsername(USERNAME).isPresent(),
          "username_taken 时库中应已存在 guoqiang");
      return;
    }

    assertEquals(200, sc, "Unexpected status body=" + raw);

    JsonNode tree = objectMapper.readTree(raw);
    assertNotNull(tree.get("id"));
    assertEquals(USERNAME, tree.get("username").asText());
    assertEquals("ACTIVE", tree.get("status").asText());
    assertTrue(userRepository.findById(tree.get("id").asLong()).isPresent());
  }
}
