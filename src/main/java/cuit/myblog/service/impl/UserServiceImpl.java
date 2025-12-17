package cuit.myblog.service.impl;

import cuit.myblog.entity.User;
import cuit.myblog.exception.BlogAPIException;
import cuit.myblog.exception.ResourceNotFoundException;
import cuit.myblog.payload.RegisterDto;
import cuit.myblog.repository.RoleRepository;
import cuit.myblog.repository.UserRepository;
import cuit.myblog.service.UserService;
import org.springframework.http.HttpStatus;
// ❌ 确保您的导入列表中不包含：org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
// 🌟 关键修复：确保这里只实现了 UserService 接口 🌟
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 构造器注入
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 实现 UserService 接口的方法 ---

    @Override
    public User registerNewUser(RegisterDto registerDto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "用户名已被占用!");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "邮箱已被注册!");
        }

        // 1. 创建 User 实体
        User user = User.builder()
                .name(registerDto.getName())
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                // 必须加密密码
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .build();

        // 2. 赋予默认角色 (例如 ROLE_USER)
        roleRepository.findByName("ROLE_USER").ifPresent(role -> {
            user.setRoles(Set.of(role));
        });

        // 3. 保存用户
        return userRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
}