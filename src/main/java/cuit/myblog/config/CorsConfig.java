package cuit.myblog.config; // 确保包名与你的项目一致

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径 (/**)
        registry.addMapping("/**")
                // 允许所有来源的访问 (开发阶段方便)
                .allowedOriginPatterns("*")
                // 允许 POST, GET 等所有 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头
                .allowedHeaders("*")
                // 允许发送 Cookie
                .allowCredentials(true);
    }
}