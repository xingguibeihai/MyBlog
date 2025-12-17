package cuit.myblog.repository;

import cuit.myblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    // Spring Security 标准加载方法
    Optional<User> findByUsernameOrEmail(String username, String email);

    // 🌟 方案 A 核心：支持用户名或邮箱查找，并强制加载角色
    @Query("SELECT u FROM User u JOIN FETCH u.roles r WHERE u.username = :username OR u.email = :username")
    Optional<User> findByUsernameOrEmailWithRoles(@Param("username") String username);
}