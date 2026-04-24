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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/** 新建角色 {@code POST /api/roles}，权限 {@code role:create}。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleCreateApiTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  @Test
  void createRole() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(() -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    String token =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), Collections.singletonList("role:create"));

    String code = "ROLE_" + System.currentTimeMillis();
    String name = "temp_role_" + code.substring(code.length() - 6);
    String desc = "created_by_test";

    String body =
        "{\"code\":\"" + code + "\",\"name\":\"" + name + "\",\"description\":\"" + desc + "\"}";

    MvcResult res =
        mockMvc
            .perform(
                post("/api/roles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
    assertNotNull(json.get("id"));
    assertEquals(code, json.get("code").asText());
    assertEquals(name, json.get("name").asText());
    assertTrue(json.get("permissions").isArray());

    long id = json.get("id").asLong();
    String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

    System.out.println();
    System.out.println("========== [角色] 新建角色 POST /api/roles ==========");
    System.out.println("HTTP 状态码: " + res.getResponse().getStatus());
    System.out.println("新建角色 id: " + id);
    System.out.println("code: " + code);
    System.out.println("name: " + name);
    System.out.println("响应体（格式化 JSON）:");
    System.out.println(pretty);
    System.out.println("核对表 sys_role：SELECT id, code, name, description FROM sys_role WHERE id=" + id + ";");
    System.out.println("====================================================");
    System.out.println();
  }
}

