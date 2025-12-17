package cuit.myblog.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // 自动生成所有字段的 Getter/Setter
@NoArgsConstructor // 生成默认的无参构造器
@AllArgsConstructor // 🚨 重新启用：生成包含所有字段的构造器
public class JwtAuthResponse {
    private String token;
    private String username;
    private String tokenType = "Bearer";
    private List<String> roles; // List<String>

    // 🎯 移除您手动添加的 public JwtAuthResponse(String token, String username) 构造器
    // 依赖 Lombok 生成的构造器
}