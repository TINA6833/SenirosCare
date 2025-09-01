package com.ryanshiun.seniorscare.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 登入失敗處理器
 * 當使用者登入失敗時，將錯誤訊息和錯誤碼附加到前端回調 URL 上。
 */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    // 我們可以共用同一個前端回調 URL
    @Value("${app.frontend.redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorCode = "login_failed";
        String errorMessage = "登入失敗，請稍後再試。";

        // 判斷例外類型是否為我們在 Service 中拋出的 DisabledException
        if (exception instanceof DisabledException) {
            errorCode = "account_disabled";
            errorMessage = "您的帳號已被停權，請聯絡客服人員。";
        }

        // 將錯誤碼和錯誤訊息附加到 URL 上，並重導向回前端
        // 記得對訊息進行 URL 編碼，以防包含特殊字元
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("error", errorCode)
                .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
