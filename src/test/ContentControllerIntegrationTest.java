package cuit.myblog.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuit.myblog.entity.Role;
import cuit.myblog.entity.User;
import cuit.myblog.payload.LoginDto;
import cuit.myblog.payload.RegisterDto;
import cuit.myblog.repository.RoleRepository;
import cuit.myblog.repository.UserRepository;
import cuit.myblog.security.JwtAuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc // 启用 MockMvc
@Transactional // 确保测试后数据库操作回滚
public class ContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 预先定义的测试用户凭证
    private final String ADMIN_USERNAME = "adminTest";
    private final String USER_USERNAME = "userTest";
    private final String PASSWORD = "password123";

    // 用于保存测试用户的 JWT Token
    private String adminToken;
    private String userToken;

    // 用于 POST 请求的模拟内容体
    private final String MOCK_CONTENT_JSON = "{\"title\": \"Test Title\", \"body\": \"Test Body\", \"userId\": 1}";

    /**
     * 在每个测试方法运行前执行，用于初始化角色和测试用户，并获取 JWT Token。
     */
    @BeforeEach
    void setup() throws Exception {
        // 1. 确保角色存在
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        // 2. 创建 ADMIN 用户
        if (!userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            User admin = new User();
            admin.setName("Admin Tester");
            admin.setUsername(ADMIN_USERNAME);
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode(PASSWORD));
            admin.setRoles(Collections.singleton(adminRole));
            userRepository.save(admin);
        }

        // 3. 创建 USER 用户
        if (!userRepository.findByUsername(USER_USERNAME).isPresent()) {
            User user = new User();
            user.setName("User Tester");
            user.setUsername(USER_USERNAME);
            user.setEmail("user@test.com");
            user.setPassword(passwordEncoder.encode(PASSWORD));
            user.setRoles(Collections.singleton(userRole));
            userRepository.save(user);
        }

        // 4. 获取 ADMIN Token
        adminToken = getTokenForUser(ADMIN_USERNAME, PASSWORD);
        assertNotNull(adminToken, "Admin Token should not be null.");

        // 5. 获取 USER Token
        userToken = getTokenForUser(USER_USERNAME, PASSWORD);
        assertNotNull(userToken, "User Token should not be null.");
    }

    /**
     * 辅助方法：模拟登录并获取 JWT Token
     */
    private String getTokenForUser(String username, String password) throws Exception {
        LoginDto loginDto = new LoginDto(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(responseContent, JwtAuthResponse.class);
        return authResponse.getAccessToken();
    }

    // --- 权限测试用例 ---

    /**
     * 测试场景 1: 未认证用户访问 POST /api/content
     * 预期结果: 401 Unauthorized (由 JwtAuthenticationEntryPoint 处理)
     */
    @Test
    void whenUnauthenticatedUserTriesToCreateContent_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOCK_CONTENT_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试场景 2: ROLE_USER 访问 POST /api/content (权限不足)
     * 预期结果: 403 Forbidden (由 @PreAuthorize("hasRole('ADMIN')") 和 JwtAccessDeniedHandler 处理)
     */
    @Test
    void whenRoleUserTriesToCreateContent_thenReturns403() throws Exception {
        mockMvc.perform(post("/api/content")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOCK_CONTENT_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试场景 3: ROLE_ADMIN 访问 POST /api/content (权限通过)
     * 预期结果: 201 Created
     */
    @Test
    void whenRoleAdminTriesToCreateContent_thenReturns201() throws Exception {
        mockMvc.perform(post("/api/content")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOCK_CONTENT_JSON))
                .andExpect(status().isCreated());
    }

    /**
     * 测试场景 4: 所有人访问 GET /api/content (无需认证)
     * 预期结果: 200 OK (由 SecurityConfig.permitAll() 允许)
     */
    @Test
    void whenAnyUserTriesToGetAllContent_thenReturns200() throws Exception {
        // 匿名访问
        mockMvc.perform(get("/api/content")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 认证用户访问
        mockMvc.perform(get("/api/content")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // 你可以根据需要添加 PUT 和 DELETE 的 403/200 测试用例...

    /**
     * 测试场景 5: ROLE_USER 访问 DELETE /api/content/{id} (假设ID=1)
     * 预期结果: 403 Forbidden
     */
    @Test
    void whenRoleUserTriesToDeleteContent_thenReturns403() throws Exception {
        mockMvc.perform(delete("/api/content/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}