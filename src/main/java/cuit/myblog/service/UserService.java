package cuit.myblog.service;

import cuit.myblog.entity.User;
import cuit.myblog.payload.RegisterDto;
// ❌ 必须删除对 org.springframework.security.core.userdetails.UserDetailsService 的导入

/**
 * 用户服务接口，专注于业务逻辑。
 */
// 🌟 关键修复：删除 extends UserDetailsService，使其不再具备安全认证职责 🌟
public interface UserService {

    /**
     * 注册新用户。
     * @param registerDto 包含注册信息的 DTO
     * @return 注册成功的 User 实体
     */
    User registerNewUser(RegisterDto registerDto);

    /**
     * 根据用户名获取 User 实体。
     * 供 CommentService 或其他需要用户信息的服务调用。
     * @param username 用户名
     * @return User 实体
     */
    User getUserByUsername(String username);

    /**
     * 根据 ID 检查用户是否存在。
     * @param userId 用户 ID
     * @return boolean
     */
    boolean existsById(Long userId);
}