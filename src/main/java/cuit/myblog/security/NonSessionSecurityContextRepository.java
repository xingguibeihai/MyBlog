package cuit.myblog.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

// 这是一个不存储、不加载 SecurityContext 的实现
public class NonSessionSecurityContextRepository implements SecurityContextRepository {

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        // 始终返回一个新的或空的 SecurityContext，强制依赖于 JWT 过滤器设置的当前线程上下文
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // 关键：不进行任何保存操作，阻止写入 Session
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        // 始终返回 false，表示没有 Session 上下文
        return false;
    }
}