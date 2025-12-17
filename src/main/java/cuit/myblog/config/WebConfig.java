package cuit.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取文件夹的绝对路径（更稳妥）
        String uploadPath = Paths.get("uploads", "blog-media").toFile().getAbsolutePath();

        /**
         * 🌟 关键映射：
         * 前端访问 URL: http://localhost:8080/media/文件名
         * 对应后端磁盘: ./uploads/blog-media/文件名
         */
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}