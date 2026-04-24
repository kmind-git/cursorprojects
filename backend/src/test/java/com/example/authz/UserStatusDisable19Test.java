package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/** 将指定用户状态改为 DISABLED（不可用）。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserStatusDisable19Test {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void disableUser19() throws Exception {
    long id = 19L;

    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:update"));

    MvcResult res =
        mockMvc
            .perform(
                patch("/api/users/" + id + "/status")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString());
    assertEquals("DISABLED", body.get("status").asText());

    System.out.println();
    System.out.println("========== [功能3] 禁用用户 PATCH /api/users/{id}/status ==========");
    System.out.println("目标用户 id: " + id);
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    System.out.println("核对表 sys_user：SELECT id, username, status FROM sys_user WHERE id=" + id + ";（应为 DISABLED）");
    System.out.println("====================================================");
    System.out.println();
  }
}

