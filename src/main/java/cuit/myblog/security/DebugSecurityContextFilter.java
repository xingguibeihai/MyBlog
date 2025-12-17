package cuit.myblog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DebugSecurityContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 关键检查点：在 JWT 过滤器之后立即检查 Security Context
        if (request.getRequestURI().contains("/comments")) {
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                System.out.println("DEBUG POST-JWT: Security Context 仍然存在！用户: " +
                        SecurityContextHolder.getContext().getAuthentication().getName());
            } else {
                System.err.println("DEBUG POST-JWT ERROR: Security Context 丢失！");
            }
        }

        filterChain.doFilter(request, response);
    }
}