package cuit.myblog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod; // 引入 HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String token = getTokenFromRequest(request);

        // 🚨 关键诊断日志：打印提取的完整 Token
        if (StringUtils.hasText(token)) {
            // 请确保 token 长度足够长，例如至少 50 个字符
            System.out.println("DEBUG JWT: 提取到的完整 Token (前50字符): " + token.substring(0, Math.min(token.length(), 50)) + "...");
        } else if (request.getRequestURI().contains("/comments") && HttpMethod.POST.matches(request.getMethod())) {
            System.out.println("DEBUG JWT: 评论 POST 请求，未找到 Token。");
        }

        // 2. 验证 Token
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

            // 🚨 诊断日志：Token 校验成功
            String username = tokenProvider.getUsername(token);
            System.out.println("DEBUG JWT: Token 校验成功，用户：" + username);

            // 3. 🚨 关键修改：捕获 UserDetails 加载异常
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 🚨 诊断日志：打印用户角色
                System.out.println("DEBUG JWT: UserDetails 加载成功。角色: " + userDetails.getAuthorities());

                // 4. 创建认证对象并存入 SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (UsernameNotFoundException ex) {
                // 如果用户在 Token 有效期内被删除，此处会触发
                System.err.println("DEBUG JWT ERROR: Token 中的用户 '" + username + "' 数据库中未找到。");
                // 确保异常被捕获，并且不会继续设置 SecurityContext
            }
        } else if (StringUtils.hasText(token)) {
            // 🚨 诊断日志：Token 存在但验证失败（过期、签名错误等）
            if (request.getRequestURI().contains("/comments") && HttpMethod.POST.matches(request.getMethod())) {
                System.out.println("DEBUG JWT: Token 存在但验证失败。");
            }
        }


        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        // 💡 优化：确保 Bearer 后面有空格
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}