package cuit.myblog.repository;

import cuit.myblog.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // 关键方法：根据角色名称查找角色（例如 "ROLE_USER"）
    Optional<Role> findByName(String name);
}