package cuit.myblog.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class JWTAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    // 🌟 1. 新增用户名字段 🌟
    private String username;
    private Set<String> roles;

    // 完整的构造函数
    public JWTAuthResponse(String accessToken, String username, Set<String> roles) {
        this.accessToken = accessToken;
        this.username = username;
        this.roles = roles;
    }

    // 省略 Getter/Setter (如果使用 Lombok，请确保有 @Getter / @Setter / @Data)
}