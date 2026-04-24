package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

/** 功能4：重置密码 {@code POST /api/users/{id}/reset-password}，权限 {@code user:update}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserResetPasswordApiTest {

  private static final List<String> PERMS = Arrays.asList("user:create", "user:update");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature4_resetPassword() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token = jwtService.issueAccessToken(admin.getId(), admin.getUsername(), PERMS);

    // 先创建临时用户
    String username = "u_pwd_" + System.currentTimeMillis();
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

    String oldHash =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalStateException("新建后应能查到用户"))
            .getPasswordHash();

    // 重置密码
    mockMvc
        .perform(
            post("/api/users/" + id + "/reset-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"ResetPass888\"}"))
        .andExpect(status().isOk());

    String newHash =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalStateException("重置后应能查到用户"))
            .getPasswordHash();

    assertNotEquals(oldHash, newHash, "password_hash 应发生变化");

    System.out.println();
    System.out.println("========== [功能4] 重置密码 POST /api/users/{id}/reset-password ==========");
    System.out.println("临时用户 id: " + id + "  username: " + username);
    System.out.println("HTTP 状态码: 200");
    System.out.println("校验点：sys_user.password_hash 已变化（不输出明文密码，也不输出哈希全文）");
    System.out.println("核对表 sys_user：");
    System.out.println("1) 重置前：SELECT id, username, password_hash FROM sys_user WHERE id=" + id + ";");
    System.out.println("2) 重置后：SELECT id, username, password_hash FROM sys_user WHERE id=" + id + ";");
    System.out.println("（两次查询的 password_hash 应不同）");
    System.out.println("====================================================");
    System.out.println();
  }
}

