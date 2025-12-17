package cuit.myblog.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.Clock; // 移除不兼容的导入

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 【关键新增】在启动时就将密钥解析为 Key 对象
    private final Key signingKey;

    // 【关键修改】构造函数中初始化 Key
    public JwtTokenProvider(@Value("${app.jwt-secret}") String jwtSecret,
                            @Value("${app.jwt-expiration-milliseconds}") long jwtExpirationDate) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationDate = jwtExpirationDate;

        // 确保 Key 对象在 Bean 初始化时就创建且不变
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        logger.info("JWT: 密钥成功初始化为 Key 对象。");
    }

    /**
     * 获取 JWT 密钥
     * 统一使用此方法获取 Key 对象，确保加密和解密/验证使用相同的机制。
     */
    private Key key() {
        return this.signingKey;
    }

    /**
     * 1. 生成 JWT Token
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    /**
     * 2. 从 JWT Token 中获取用户名 (Subject)
     */
    public String getUsername(String token) {
        // 使用 Jwts.parser().setSigningKey(Key) 的现代方式
        Claims claims = Jwts.parser()
                .setSigningKey(key()) // <-- 使用 Key 对象
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 保留你原有的方法名以兼容旧代码 (如果不需要，可以移除)
    public String getUsernameFromJWT(String token){
        return getUsername(token);
    }

    /**
     * 3. 验证 JWT Token 是否有效
     * 返回 true 表示有效，返回 false 并记录详细日志表示无效。
     */
    public boolean validateToken(String token) {
        try {
            // 设置 5 分钟 (300 秒) 的时钟偏差容忍度，解决时间同步问题
            long clockSkewSeconds = 300;

            // 使用 Jwts.parser() 构建解析器，设置 Key 和容忍度
            JwtParser parser = Jwts.parser()
                    .setSigningKey(key()) // 使用 Key 对象
                    .setAllowedClockSkewSeconds(clockSkewSeconds) // 【关键修改】设置时钟偏差容忍度
                    .build();

            parser.parseClaimsJws(token);

            return true;

        } catch (SignatureException ex) {
            // 签名错误：密钥不匹配
            logger.error("Invalid JWT signature (无效的 JWT 签名): {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // 格式错误：Token 结构不对
            logger.error("Invalid JWT token (非法的 JWT Token): {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // 过期错误：Token 时间戳已过
            logger.error("Expired JWT token (JWT Token 已过期): {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // 不支持的 Token 类型
            logger.error("Unsupported JWT token (不支持的 JWT Token): {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Claims 字符串为空等参数错误
            logger.error("JWT claims string is empty or invalid (JWT Claims 字符串为空或无效): {}", ex.getMessage());
        }
        return false;
    }
}