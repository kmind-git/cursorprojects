package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.authz.domain.UserEntity;
import com.example.authz.repo.UserRepository;
import com.example.authz.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 功能6：删除用户 {@code DELETE /api/users/{id}}，权限 {@code user:delete}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserDelete24ApiTest {

  private static final long USER_ID = 24L;

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature6_deleteUser24() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));

    userRepository
        .findById(USER_ID)
        .orElseThrow(
            () -> new IllegalStateException("需要用户 id=" + USER_ID + "，请核对表 sys_user（当前不存在）"));

    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:delete"));

    mockMvc
        .perform(delete("/api/users/" + USER_ID).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    assertFalse(userRepository.findById(USER_ID).isPresent(), "删除后 sys_user 不应存在该 id");

    System.out.println();
    System.out.println("========== [功能6] 删除用户 DELETE /api/users/{id} ==========");
    System.out.println("目标用户 id: " + USER_ID);
    System.out.println("HTTP 状态码: 200");
    System.out.println("核对表 sys_user：SELECT id, username, status FROM sys_user WHERE id=" + USER_ID + ";（应为空）");
    System.out.println("核对表 sys_user_role：SELECT * FROM sys_user_role WHERE user_id=" + USER_ID + ";（应为空）");
    System.out.println(
        "核对表 sys_refresh_token：SELECT * FROM sys_refresh_token WHERE user_id="
            + USER_ID
            + ";（如存在记录，可能导致删除失败，需要先清理或配置级联）");
    System.out.println("====================================================");
    System.out.println();

    Path out = Paths.get("target", "user24-delete-last.txt");
    String content =
        "HTTP=200\n"
            + "userId=" + USER_ID + "\n"
            + "verifySql1=SELECT id, username, status FROM sys_user WHERE id=" + USER_ID + ";\n"
            + "verifySql2=SELECT * FROM sys_user_role WHERE user_id=" + USER_ID + ";\n"
            + "verifySql3=SELECT * FROM sys_refresh_token WHERE user_id=" + USER_ID + ";\n";
    Files.createDirectories(out.getParent());
    Files.write(out, content.getBytes(StandardCharsets.UTF_8));
  }
}

