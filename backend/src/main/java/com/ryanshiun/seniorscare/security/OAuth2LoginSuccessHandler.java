package com.ryanshiun.seniorscare.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 登入成功處理器
 * 當使用者登入成功時，將 JWT 附加到前端回調 URL 上。
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 從 application.properties 讀取您的前端接收 token 的 URL
    @Value("${app.frontend.redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 取得我們自訂的 Principal 物件
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User customOAuth2User)) {
            // 如果不是預期的類型，可以記錄錯誤或直接返回
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "無法處理的使用者類型");
            return;
        }

        // 2. 從 CustomOAuth2User 中取得 member_id
        Integer memberId = customOAuth2User.getMemberId();

        // 3. 使用 member_id 產生 JWT
        String token = jwtTokenProvider.generateMemberToken(String.valueOf(memberId));

        // 4. 建立重導向 URL
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", token)
                .build().toUriString();

        // 5. 清除屬性並執行重導向
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
