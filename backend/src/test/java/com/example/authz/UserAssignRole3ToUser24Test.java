package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.authz.domain.RoleEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.repo.RoleRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 将数据库中 id=3 的角色赋给用户 id=24（通过角色 code 调用 API）。
 *
 * <p>注意：服务端实现会先清空该用户已有角色，再仅保留请求中列出的角色。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAssignRole3ToUser24Test {

  private static final long USER_ID = 24L;
  private static final long ROLE_ID = 3L;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;

  @Test
  void assignRole3ToUser24() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    userRepository
        .findById(USER_ID)
        .orElseThrow(
            () -> new IllegalStateException("需要用户 id=" + USER_ID + "，请核对表 sys_user"));
    RoleEntity role =
        roleRepository
            .findById(ROLE_ID)
            .orElseThrow(
                () -> new IllegalStateException("需要角色 id=" + ROLE_ID + "，请核对表 sys_role"));

    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("user:assignRole"));

    String reqBody = "{\"roleCodes\":[\"" + role.getCode() + "\"]}";
    MvcResult res =
        mockMvc
            .perform(
                post("/api/users/" + USER_ID + "/roles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqBody))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString());
    assertTrue(
        body.get("roles").toString().contains(role.getCode()),
        "响应 roles 应包含角色 code=" + role.getCode());

    System.out.println();
    System.out.println("========== 分配角色 POST /api/users/{id}/roles ==========");
    System.out.println("用户 id: " + USER_ID + "  角色 id: " + ROLE_ID + "  角色 code: " + role.getCode());
    System.out.println("请求体: " + reqBody);
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    System.out.println(
        "核对表 sys_user_role：SELECT * FROM sys_user_role WHERE user_id=" + USER_ID + ";");
    System.out.println(
        "（role_id 应为 "
            + ROLE_ID
            + "；若该用户原先有其他角色，已被清空，仅保留本次请求中的角色）");
    System.out.println("====================================================");
    System.out.println();

    Path out = Paths.get("target", "user24-assign-role3-last.txt");
    String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
    String content =
        "HTTP="
            + res.getResponse().getStatus()
            + "\nuserId="
            + USER_ID
            + "\nroleId="
            + ROLE_ID
            + "\nroleCode="
            + role.getCode()
            + "\nrequestJson="
            + reqBody
            + "\nresponseJson="
            + pretty
            + "\nverifySql=SELECT * FROM sys_user_role WHERE user_id="
            + USER_ID
            + ";\n";
    Files.createDirectories(out.getParent());
    Files.write(out, content.getBytes(StandardCharsets.UTF_8));
  }
}
