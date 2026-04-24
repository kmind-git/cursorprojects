package com.example.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 用户管理 API：按步骤拆成多个 {@link Test}，按 {@link Order} 1→7 执行。
 *
 * <p><strong>如何边测边查库</strong>
 *
 * <ul>
 *   <li><strong>一次跑完全部</strong>：适合看控制台每步输出；注意第 7 步会删用户，跑完后库里看不到中间态，只能依赖日志。
 *   <li><strong>分多次、每步后查库</strong>：先只跑步骤 2，控制台记下 {@code id}；在库里核对后，再只跑步骤 3（或 4…），并在 VM options /
 *       Maven 加上 {@code -Dauthz.flow.userId=该id}。步骤 1、2 可单独跑，无需该参数；步骤 3～7 单独跑时必须带参数或同一次运行里已执行过步骤 2。
 * </ul>
 *
 * <p>核对表：{@code sys_user}、{@code sys_user_role}、{@code sys_role}；删除时若存在 {@code sys_refresh_token} 等外键需先处理。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementApiOrderedIT {

  /** 分多次运行步骤 3～7 时，传入步骤 2 打印的 user id。 */
  private static final String FLOW_USER_ID_PROP = "authz.flow.userId";

  private static final List<String> USER_MGMT_PERMS =
      Arrays.asList(
          "user:read", "user:create", "user:update", "user:delete", "user:assignRole");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserRepository userRepository;

  private String accessToken;
  /** 第 2 步创建的用户 id，供后续步骤使用。 */
  private Long flowUserId;
  /** 第 2 步创建的用户名，便于在库中 WHERE。 */
  private String flowUsername;

  @BeforeAll
  void issueAdminToken() {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseThrow(
                () -> new IllegalStateException("需要种子用户 admin，请核对表 sys_user"));
    accessToken =
        jwtService.issueAccessToken(
            admin.getId(), admin.getUsername(), USER_MGMT_PERMS);
  }

  private void banner(String title) {
    System.out.println();
    System.out.println("---------- " + title + " ----------");
  }

  /**
   * 同一次运行中优先用步骤 2 写入的 {@link #flowUserId}；分次运行时可用 {@value #FLOW_USER_ID_PROP}。
   */
  private long requireFlowUserId() {
    if (flowUserId != null) {
      return flowUserId;
    }
    String p = System.getProperty(FLOW_USER_ID_PROP);
    if (p != null && !p.trim().isEmpty()) {
      return Long.parseLong(p.trim());
    }
    throw new AssertionError(
        "请先运行步骤 2，或对步骤 3～7 设置 JVM：-D"
            + FLOW_USER_ID_PROP
            + "=<用户id>（步骤 2 会打印 id）");
  }

  @Test
  @Order(1)
  @DisplayName("1. 列表 GET /api/users")
  void step01_listUsers() throws Exception {
    banner("步骤 1：列表（可在此后查库核对用户数）");
    MvcResult listResult =
        mockMvc
            .perform(
                get("/api/users").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
    assertTrue(listJson.isArray(), "列表应为 JSON 数组");
    int listCount = listJson.size();
    System.out.println(
        "HTTP 200，当前用户数="
            + listCount
            + "。核对表 sys_user：SELECT id, username, status FROM sys_user;");
    assertTrue(listCount >= 1, "至少应有 admin 等用户");
  }

  @Test
  @Order(2)
  @DisplayName("2. 新建 POST /api/users")
  void step02_createUser() throws Exception {
    banner("步骤 2：新建用户（记下 id / username，后续步骤依赖此用户）");
    flowUsername = "um_step_" + System.currentTimeMillis();
    String createBody =
        "{\"username\":\"" + flowUsername + "\",\"password\":\"InitPass999\"}";
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
    flowUserId = created.get("id").asLong();
    assertEquals(flowUsername, created.get("username").asText());
    assertEquals("ACTIVE", created.get("status").asText());
    System.out.println(
        "HTTP 200，新建 id="
            + flowUserId
            + " username="
            + flowUsername
            + " status=ACTIVE。");
    System.out.println(
        "核对表 sys_user：SELECT * FROM sys_user WHERE id="
            + flowUserId
            + " OR username='"
            + flowUsername
            + "';");
    System.out.println(
        "（建议用）SELECT * FROM sys_user WHERE username='" + flowUsername + "';");
    System.out.println(
        "若分多次运行：步骤 3～7 单独执行时请加 -D"
            + FLOW_USER_ID_PROP
            + "="
            + flowUserId);
  }

  @Test
  @Order(3)
  @DisplayName("3. 禁用 PATCH .../status DISABLED")
  void step03_statusDisabled() throws Exception {
    banner("步骤 3：改为 DISABLED");
    long uid = requireFlowUserId();
    String body =
        mockMvc
            .perform(
                patch("/api/users/" + uid + "/status")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertEquals("DISABLED", objectMapper.readTree(body).get("status").asText());
    System.out.println("HTTP 200，响应 status=DISABLED。");
    System.out.println(
        "核对表 sys_user：SELECT id, status FROM sys_user WHERE id=" + uid + " — 应为 DISABLED");
  }

  @Test
  @Order(4)
  @DisplayName("4. 启用 PATCH .../status ACTIVE")
  void step04_statusActive() throws Exception {
    banner("步骤 4：改回 ACTIVE");
    long uid = requireFlowUserId();
    mockMvc
        .perform(
            patch("/api/users/" + uid + "/status")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
        .andExpect(status().isOk());
    System.out.println("HTTP 200。");
    System.out.println(
        "核对表 sys_user：SELECT id, status FROM sys_user WHERE id=" + uid + " — 应为 ACTIVE");
  }

  @Test
  @Order(5)
  @DisplayName("5. 重置密码 POST .../reset-password")
  void step05_resetPassword() throws Exception {
    banner("步骤 5：重置密码");
    long uid = requireFlowUserId();
    mockMvc
        .perform(
            post("/api/users/" + uid + "/reset-password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"ResetPass888\"}"))
        .andExpect(status().isOk());
    System.out.println("HTTP 200（空响应体）。");
    System.out.println(
        "核对表 sys_user：对比 id="
            + uid
            + " 的 password_hash 是否已更新（无明文列，可看哈希变化）");
  }

  @Test
  @Order(6)
  @DisplayName("6. 分配角色 POST .../roles")
  void step06_assignRoles() throws Exception {
    banner("步骤 6：分配 SUPER_ADMIN");
    long uid = requireFlowUserId();
    MvcResult assignResult =
        mockMvc
            .perform(
                post("/api/users/" + uid + "/roles")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"roleCodes\":[\"SUPER_ADMIN\"]}"))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode afterRoles = objectMapper.readTree(assignResult.getResponse().getContentAsString());
    assertTrue(
        afterRoles.get("roles").toString().contains("SUPER_ADMIN"),
        "角色应包含 SUPER_ADMIN");
    System.out.println("HTTP 200，响应 roles 含 SUPER_ADMIN。");
    System.out.println(
        "核对表 sys_user_role：SELECT * FROM sys_user_role WHERE user_id=" + uid + ";");
    System.out.println(
        "核对表 sys_role：SELECT id, code FROM sys_role WHERE code='SUPER_ADMIN';（与上一表的 role_id 对应）");
  }

  @Test
  @Order(7)
  @DisplayName("7. 删除 DELETE /api/users/{id}")
  void step07_deleteUser() throws Exception {
    banner("步骤 7：删除用户");
    long uid = requireFlowUserId();
    mockMvc
        .perform(
            delete("/api/users/" + uid).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
    assertFalse(userRepository.findById(uid).isPresent());
    System.out.println("HTTP 200，本地仓库已查无该 id。");
    System.out.println(
        "核对表 sys_user：无 id=" + uid + "；sys_user_role：无 user_id=" + uid);
    flowUserId = null;
    flowUsername = null;
  }
}
