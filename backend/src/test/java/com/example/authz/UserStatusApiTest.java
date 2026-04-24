package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.authz.domain.UserEntity;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/** 功能3：修改用户状态 {@code PATCH /api/users/{id}/status}，权限 {@code user:update}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserStatusApiTest {

  private static final List<String> PERMS = Arrays.asList("user:create", "user:update");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature3_updateUserStatus() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token = jwtService.issueAccessToken(admin.getId(), admin.getUsername(), PERMS);

    // 先创建一个临时用户，拿到 id
    String username = "u_status_" + System.currentTimeMillis();
    MvcResult createdRes =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + username + "\",\"password\":\"InitPass999\"}"))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode created = objectMapper.readTree(createdRes.getResponse().getContentAsString());
    assertNotNull(created.get("id"));
    long id = created.get("id").asLong();

    // 改为 DISABLED
    MvcResult disRes =
        mockMvc
            .perform(
                patch("/api/users/" + id + "/status")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode dis = objectMapper.readTree(disRes.getResponse().getContentAsString());
    assertEquals("DISABLED", dis.get("status").asText());

    // 再改回 ACTIVE（便于你核对两次变化）
    MvcResult actRes =
        mockMvc
            .perform(
                patch("/api/users/" + id + "/status")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"ACTIVE\"}"))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode act = objectMapper.readTree(actRes.getResponse().getContentAsString());
    assertEquals("ACTIVE", act.get("status").asText());

    System.out.println();
    System.out.println("========== [功能3] 修改用户状态 PATCH /api/users/{id}/status ==========");
    System.out.println("临时用户 id: " + id + "  username: " + username);
    System.out.println("1) PATCH -> DISABLED  HTTP=" + disRes.getResponse().getStatus());
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dis));
    System.out.println("核对表 sys_user：SELECT id, username, status FROM sys_user WHERE id=" + id + ";（应为 DISABLED）");
    System.out.println("2) PATCH -> ACTIVE    HTTP=" + actRes.getResponse().getStatus());
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(act));
    System.out.println("核对表 sys_user：SELECT id, username, status FROM sys_user WHERE id=" + id + ";（应为 ACTIVE）");
    System.out.println("====================================================");
    System.out.println();
  }
}

