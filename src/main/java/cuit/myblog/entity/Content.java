package cuit.myblog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set; // 🌟 新增导入

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "content")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDateTime publishedAt = LocalDateTime.now();

    // 🌟 关键修复点 🌟
    // 配置与 Comment 实体的一对多关系，并启用级联删除和孤儿移除
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();
    // 注意：您需要确保您的 Comment 实体类中有一个名为 'content' 的字段用于 mappedBy

    // 我们需要一个不带 comments 字段的构造函数，以便 JPA 和 Lombok 正常工作
    // 因为您使用了 @AllArgsConstructor, 建议手动添加或使用 @Builder
    // 为了简化，这里移除 @AllArgsConstructor，让 Lombok 生成默认的，并手动提供一个不含ID和集合的构造函数
    public Content(String title, String body, String author) {
        this.title = title;
        this.body = body;
        this.author = author;
        this.publishedAt = LocalDateTime.now();
    }
}