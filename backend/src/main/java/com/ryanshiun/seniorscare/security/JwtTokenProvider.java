package com.ryanshiun.seniorscare.security;

import com.ryanshiun.seniorscare.member.dto.enums.TokenState;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    /**
     * 從 application.properties 讀取 Base64 格式的金鑰
     */
    @Value("${app.jwt.secret}")
    private String base64Secret;

    /**
     * 從 application.properties 讀取的過期毫秒
     */
    @Value("${app.jwt.expiration-in-ms}")
    private long jwtExpirationInMs;

    /**
     * 解碼後的 HMAC-SHA 金鑰
     */
    private Key secretKey;

    @PostConstruct
    public void init() {
        // Base64 解碼並產生金鑰
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     *   產生員工登入的 JWT：
     * - 子載荷 subject: 會員唯一識別碼 (例如 Line User ID)
     * - 自訂 claims: roles
     * - 簽章演算法: HS256 (預設)
     */
    public String generateEmployeeToken(Authentication authentication) {
        String username = authentication.getName();

        // 將 authorities 轉成 String list
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiryDate = Date.from(now.plusMillis(jwtExpirationInMs));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("type", TokenState.EMPLOYEE)
                .issuedAt(issuedAt)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     *   產生會員登入的 JWT：
     * - 子載荷 subject: 例如 Line User ID
     * - 自訂 claims: type
     */
    public String generateMemberToken(String memberId) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiryDate = Date.from(now.plusMillis(jwtExpirationInMs));

        return Jwts.builder()
                .subject(memberId)
                .claim("type", TokenState.MEMBER) // 指定類型為 MEMBER
                .issuedAt(issuedAt)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 驗證 JWT 是否有效
     */
    public TokenState validateToken(String token) {
        try {
            Claims claims = this.parseClaims(token);
            String type = (String) claims.get("type");
            if (type == null) {
                return TokenState.INVALID;
            }
            return TokenState.valueOf(type.toUpperCase());
        } catch (JwtException | IllegalArgumentException ex) {
            // JwtException 包含過期、簽章錯誤等
            return TokenState.INVALID;
        }
    }

    /**
     * 從 JWT 取得 員工的 emp_id 或 會員的 line_id
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     *  解析 claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     *  從 JWT 建立 Spring Authentication
     */
    @SuppressWarnings("unchecked")
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        String type = (String) claims.get("type");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 如果是員工
        if (Objects.equals(TokenState.EMPLOYEE.toString(), type)) {
            List<String> roles = claims.get("roles", List.class);
            authorities.addAll(roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        } else if (Objects.equals(TokenState.MEMBER.toString(), type)) {
            // 如果是會員給一個固定的角色
            authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }

        // 因前端只需驗證，不需密碼，比對時傳空密碼即可
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")  // 密碼不在此使用
                .authorities(authorities)
                .build();


        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}