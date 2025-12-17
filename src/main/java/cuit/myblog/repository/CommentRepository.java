package cuit.myblog.repository;

import cuit.myblog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 根据文章ID查询评论，按时间倒序排列
    List<Comment> findByContentIdOrderByPublishedAtDesc(Long contentId);
    int countByContentId(Long contentId); // JPA 自动实现计数查询
}