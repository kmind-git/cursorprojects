package com.example.authz;

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

/** 功能5：分配角色 {@code POST /api/users/{id}/roles}，权限 {@code user:assignRole}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAssignRolesApiTest {

  private static final List<String> PERMS = Arrays.asList("user:create", "user:assignRole");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void feature5_assignRoles() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token = jwtService.issueAccessToken(admin.getId(), admin.getUsername(), PERMS);

    // 先创建临时用户
    String username = "u_roles_" + System.currentTimeMillis();
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

    // 分配 SUPER_ADMIN
    MvcResult res =
        mockMvc
            .perform(
                post("/api/users/" + id + "/roles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"roleCodes\":[\"SUPER_ADMIN\"]}"))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString());
    assertTrue(body.get("roles").toString().contains("SUPER_ADMIN"), "roles 应包含 SUPER_ADMIN");

    System.out.println();
    System.out.println("========== [功能5] 分配角色 POST /api/users/{id}/roles ==========");
    System.out.println("临时用户 id: " + id + "  username: " + username);
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    System.out.println("核对表 sys_role：SELECT id, code FROM sys_role WHERE code='SUPER_ADMIN';");
    System.out.println("核对表 sys_user_role：SELECT * FROM sys_user_role WHERE user_id=" + id + ";");
    System.out.println("（sys_user_role.role_id 应等于 sys_role 中 SUPER_ADMIN 的 id）");
    System.out.println("====================================================");
    System.out.println();

    Path out = Paths.get("target", "user-assign-roles-last.txt");
    String pretty =
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
    String content =
        "HTTP=" + res.getResponse().getStatus() + "\n"
            + "id=" + id + "\n"
            + "username=" + username + "\n"
            + "responseJson=" + pretty + "\n"
            + "verifySql1=SELECT id, code FROM sys_role WHERE code='SUPER_ADMIN';\n"
            + "verifySql2=SELECT * FROM sys_user_role WHERE user_id=" + id + ";\n";
    Files.createDirectories(out.getParent());
    Files.write(out, content.getBytes(StandardCharsets.UTF_8));
  }
}

