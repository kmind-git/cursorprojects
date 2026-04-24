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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/** 功能2：新建用户 {@code POST /api/users}，权限 {@code user:create}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserCreateApiTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature2_createUser() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:create"));

    String username = "u_create_" + System.currentTimeMillis();
    String reqBody = "{\"username\":\"" + username + "\",\"password\":\"InitPass999\"}";

    MvcResult res =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqBody))
            .andExpect(status().isOk())
            .andReturn();

    String raw = res.getResponse().getContentAsString();
    JsonNode tree = objectMapper.readTree(raw);

    assertNotNull(tree.get("id"));
    assertEquals(username, tree.get("username").asText());
    assertEquals("ACTIVE", tree.get("status").asText());
    assertTrue(tree.get("roles").isArray());

    long id = tree.get("id").asLong();
    assertTrue(userRepository.findById(id).isPresent(), "库中应存在该用户");

    String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
    System.out.println();
    System.out.println("========== [功能2] 新建用户 POST /api/users ==========");
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("新建用户 id: " + id);
    System.out.println("新建用户名: " + username);
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(pretty);
    System.out.println(
        "核对表 sys_user：SELECT id, username, status, password_hash FROM sys_user WHERE id=" + id + ";");
    System.out.println("====================================================");
    System.out.println();

    Path out = Paths.get("target", "user-create-last.txt");
    String content =
        "HTTP=" + res.getResponse().getStatus() + "\n"
            + "id=" + id + "\n"
            + "username=" + username + "\n"
            + "responseJson=" + pretty + "\n"
            + "verifySql=SELECT id, username, status, password_hash FROM sys_user WHERE id=" + id + ";\n";
    Files.createDirectories(out.getParent());
    Files.write(out, content.getBytes(StandardCharsets.UTF_8));
  }
}

