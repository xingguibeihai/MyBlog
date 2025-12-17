package cuit.myblog.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 实现了 Spring Security 的 UserDetails 接口，用于封装 JWT Token 中的用户信息。
 * 允许使用 @AuthenticationPrincipal 在 Controller 中直接获取当前用户身份。
 */
@Getter
public class JwtUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    // 💡 辅助方法：从 JWT Payload 中获取用户名和角色信息后，构造 JwtUserDetails
    public static JwtUserDetails create(Long id, String username, List<GrantedAuthority> authorities) {
        return new JwtUserDetails(
                id,
                username,
                authorities
        );
    }

    // --- UserDetails 接口实现 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // 在基于 Token 的认证中，通常不存储密码
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}