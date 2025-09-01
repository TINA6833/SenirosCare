package com.ryanshiun.seniorscare.security;

import com.ryanshiun.seniorscare.member.dto.enums.TokenState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {


        // CORS Preflight 請求通常是 OPTIONS method，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();

        // 登入 & 忘記密碼一律放行
        if (path.startsWith("/api/auth/") || path.startsWith("/api/pwdReset")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 取出 Bearer token
        String token = resolveToken(request);

        // 如果沒有 token，直接放行，交給 security 做後續判斷
        if(Strings.isBlank(token)){
            filterChain.doFilter(request, response);
            return;
        }

        TokenState tokenState = jwtTokenProvider.validateToken(token);

        // 驗證 & 設定 SecurityContext
        if (tokenState != TokenState.INVALID) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 繼續執行其它 filter
        filterChain.doFilter(request, response);
    }

    /**
     * 從 Header 取得並截掉 "Bearer " 前綴
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
