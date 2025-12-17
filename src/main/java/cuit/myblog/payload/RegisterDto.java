package cuit.myblog.payload;

import lombok.Data;

@Data // Lombok 注解，自动生成 Getter, Setter, toString, equals等方法
public class RegisterDto {
    private String name;
    private String username;
    private String email;
    private String password;
}