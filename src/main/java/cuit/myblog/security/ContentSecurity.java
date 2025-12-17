package cuit.myblog.security;

import cuit.myblog.repository.ContentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("contentSecurity") // 💡 注册为一个 Spring Bean，名称为 contentSecurity
public class ContentSecurity {

    private final ContentRepository contentRepository;

    public ContentSecurity(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    /**
     * 检查当前用户是否有权修改/删除指定 ID 的文章。
     * @param contentId 要操作的文章 ID
     * @return true 如果用户是管理员或文章作者，否则 false
     */
    public boolean isContentOwnerOrAdmin(Long contentId) {
        // 1. 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // 2. 检查是否为管理员 (ROLE_ADMIN)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true; // 管理员可以操作所有内容
        }

        // 3. 检查是否为文章作者
        // 从数据库查找文章，并获取作者
        return contentRepository.findById(contentId)
                .map(content -> content.getAuthor().equals(currentUsername)) // 比较文章作者是否是当前登录用户
                .orElse(false); // 如果文章不存在，则返回 false (或可根据需求抛出异常)
    }
}