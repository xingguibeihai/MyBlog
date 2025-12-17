package cuit.myblog;

import org.springframework.boot.SpringApplication;
import org.modelmapper.ModelMapper; // 导入
import org.springframework.context.annotation.Bean; // 导入
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// 🎯 目标：让 Spring 扫描到所有带 @Entity 的类
// 确保这里的路径 "cuit.myblog.entity" 是你的 User.java 和 Content.java 所在的包路径！
@EntityScan("cuit.myblog.entity")
// 🎯 目标：让 Spring 扫描到所有继承 JpaRepository 的接口
// 确保这里的路径 "cuit.myblog.repository" 是你的 UserRepository 和 ContentRepository 所在的包路径！
@EnableJpaRepositories("cuit.myblog.repository")
public class MyBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBlogApplication.class, args);
    }

    // 💡 关键新增：将 ModelMapper 声明为一个 Spring Bean
    // 这样 Spring 容器就知道如何实例化它，并注入到 CommentServiceImpl 中
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}