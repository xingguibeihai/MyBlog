package cuit.myblog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 存储角色名称，例如 "ROLE_ADMIN", "ROLE_USER"
    @Column(length = 60, nullable = false, unique = true)
    private String name;

    // 无参构造函数是 JPA 规范的要求
    public Role() {}

    public Role(String name) {
        this.name = name;
    }
}