package cuit.myblog.controller;

import cuit.myblog.payload.JWTAuthResponse;
import cuit.myblog.payload.LoginDto;
import cuit.myblog.payload.RegisterDto;
import cuit.myblog.security.JwtTokenProvider;
import cuit.myblog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.userdetails.UserDetails; // 🌟 导入 UserDetails

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    /**
     * 处理用户登录请求，返回JWT Token和Username
     * 路径: /auth/login
     */
    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JWTAuthResponse> login(@RequestBody LoginDto loginDto){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        String currentUsername = authentication.getName(); // 简化获取用户名

        // 🌟 关键修复点：提取用户的所有角色名 🌟
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 🌟 使用新的构造函数返回 Token, 用户名 和 角色 🌟
        return ResponseEntity.ok(new JWTAuthResponse(token, currentUsername, roles));
    }

    // ... (registerUser 方法保持不变)
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDto registerDto){
        userService.registerNewUser(registerDto);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }
}