package cuit.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 强制：当访问 / 时，转发给 /login.html
        registry.addViewController("/").setViewName("forward:/login.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 显式指定静态资源寻址路径：把所有 /** 请求映射到 jar 包内的 static 文件夹
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}