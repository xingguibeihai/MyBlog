package cuit.myblog.repository; // 确保包名正确

import cuit.myblog.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // 自定义查询方法：根据作者查找所有内容
    Page<Content> findByAuthor(String author, Pageable pageable);

    // 自定义查询方法：根据标题模糊搜索
    List<Content> findByTitleContaining(String title);

    // 💡 新增：根据 Title 或 Body 进行模糊搜索（并支持分页和排序）
    Page<Content> findByTitleContainingOrBodyContaining(String title, String body, Pageable pageable);
}