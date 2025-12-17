package cuit.myblog.config;

import cuit.myblog.entity.User;
import cuit.myblog.entity.Role;
import cuit.myblog.repository.UserRepository;
import cuit.myblog.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // 导入这个包

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    // 默认管理员信息
    private static final String ADMIN_USERNAME = "super_admin";
    private static final String ADMIN_PASSWORD = "admin_password_123";
    private static final String ADMIN_NAME = "超级管理员";
    private static final String ADMIN_EMAIL = "admin@myblog.com";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 构造函数注入所有依赖
    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 【关键修复】使用 @Transactional 注解确保整个方法在一个事务中执行。
     * 这样，通过 findByName 查找到的 Role 实体（adminRole/userRole）
     * 将保持在托管态（Managed State），直到 User 实体保存完毕，
     * 从而解决了 "detached entity passed to persist" 错误。
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // --- 1. 角色初始化 ---
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_USER");
            System.out.println("✅ Initial Role: ROLE_USER created.");
            return roleRepository.save(role);
        });

        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_ADMIN");
            System.out.println("✅ Initial Role: ROLE_ADMIN created.");
            return roleRepository.save(role);
        });

        // --- 2. 管理员用户初始化 ---
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {

            System.out.println("--- 正在创建默认管理员用户: " + ADMIN_USERNAME + " ---");

            User admin = new User();
            admin.setUsername(ADMIN_USERNAME);

            // 赋值 name 和 email，确保不违反 NOT NULL 约束
            admin.setName(ADMIN_NAME);
            admin.setEmail(ADMIN_EMAIL);

            // 密码加密
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));

            // 设置角色
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole); // adminRole 是托管态
            admin.setRoles(roles);

            userRepository.save(admin);

            System.out.println("--- 管理员用户创建成功！请使用用户名: " + ADMIN_USERNAME + " 登录 ---");
        } else {
            System.out.println("--- 管理员用户 " + ADMIN_USERNAME + " 已存在，跳过创建 ---");
        }
    }
}