package cuit.myblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        // 返回 "forward:/index.html" 告诉 Spring Boot 转向静态资源文件夹下的 index.html
        return "forward:/login.html";
    }
}