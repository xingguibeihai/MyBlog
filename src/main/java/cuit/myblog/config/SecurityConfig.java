package cuit.myblog.config;

import cuit.myblog.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final DebugSecurityContextFilter debugFilter;

    // 注入依赖
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler,
                          DebugSecurityContextFilter debugFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.debugFilter = debugFilter;
    }

    // 关键：使用自定义的 SecurityContextRepository Bean
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new NonSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository
    ) throws Exception {
        http
                // 1. 禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 启用 CORS
                .cors(Customizer.withDefaults())

                // 3. 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 4. 配置授权规则
// 4. 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有 OPTIONS 预检请求通过
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🌟 核心修复：放行所有的静态页面和资源 🌟
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",    // 必须显式放行 login.html
                                "/register.html", // 必须显式放行 register.html
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/fonts/**"
                        ).permitAll()

// 放行媒体资源
                                .requestMatchers("/media/**").permitAll()

                                // 放行认证接口
                                .requestMatchers("/auth/**", "/api/auth/**").permitAll()

                        // GET 请求公开 (获取文章列表和评论列表)
                        .requestMatchers(HttpMethod.GET, "/api/content/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/content/*/comments").permitAll()

                        // POST 评论需要认证
                        .requestMatchers(HttpMethod.POST, "/api/content/*/comments").authenticated()

                        // 其他所有请求必须认证
                        .anyRequest().authenticated()
                )

                // 5. 设置为无状态 session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 6. 过滤器顺序
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(debugFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 定义 CORS 配置源
     * 解决前端跨域请求被浏览器阻止的问题。
     */
// 在 cuit.myblog.config.SecurityConfig.java 中

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌟 关键修复：添加 "null" 源，以允许直接在浏览器中打开 file:/// 路径的 HTML 文件进行测试
        List<String> allowedOrigins = new java.util.ArrayList<>();
        allowedOrigins.add("http://localhost:3000"); // 您的前端开发服务器
        allowedOrigins.add("http://127.0.0.1:3000"); // 您的前端开发服务器
        allowedOrigins.add("null"); // 允许 file:/// 协议（即直接双击 HTML 文件）

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 认证管理器 Bean
     * 用于处理登录认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 密码编码器 Bean
     * 用于加密和验证用户密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}