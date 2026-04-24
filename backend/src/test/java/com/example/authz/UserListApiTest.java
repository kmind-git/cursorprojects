package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/** 功能1：用户列表 {@code GET /api/users}，权限 {@code user:read}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserListApiTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature1_listUsers() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:read"));

    MvcResult res =
        mockMvc
            .perform(get("/api/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

    String raw = res.getResponse().getContentAsString();
    JsonNode tree = objectMapper.readTree(raw);
    assertTrue(tree.isArray(), "响应应为 JSON 数组");
    assertTrue(tree.size() >= 1, "至少包含一条记录");

    String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
    System.out.println();
    System.out.println("========== [功能1] 用户列表 GET /api/users ==========");
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("用户条数: " + tree.size());
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(pretty);
    System.out.println("核对表 sys_user：SELECT id, username, status FROM sys_user;");
    System.out.println("核对表 sys_user_role/sys_role：可按 user_id 联表核对角色。");
    System.out.println("====================================================");
    System.out.println();
  }
}

