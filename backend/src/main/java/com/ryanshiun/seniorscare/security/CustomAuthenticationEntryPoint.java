
package com.ryanshiun.seniorscare.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用於處理員工登入 帳號密碼錯誤 -> 401 被停權 -> 403
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 【修改】深入檢查例外鏈，判斷根本原因是否為 DisabledException
        if (hasCauseOfType(authException, DisabledException.class)) {
            // 帳號被停權，回傳 403 Forbidden
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpServletResponse.SC_FORBIDDEN);
            body.put("error", "Forbidden");
            // 使用 authException.getMessage() 可以得到更具體的錯誤訊息，例如 "Employee is inactive..."
            body.put("message", "您的帳號已被停權: " + authException.getMessage());
            body.put("path", request.getServletPath());

            objectMapper.writeValue(response.getOutputStream(), body);
        } else {
            // 對於所有其他認證錯誤（如密碼錯誤、用戶不存在等），返回 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            body.put("error", "Unauthorized");
            body.put("message", "認證失敗: " + authException.getMessage());
            body.put("path", request.getServletPath());

            objectMapper.writeValue(response.getOutputStream(), body);
        }
    }

    /**
     * 【新增】遞迴檢查例外原因鏈的輔助方法
     * @param throwable 要檢查的例外
     * @param type 目標例外類型
     * @return 如果在原因鏈中找到目標類型，則返回 true
     */
    private boolean hasCauseOfType(Throwable throwable, Class<? extends Throwable> type) {
        if (throwable == null) {
            return false;
        }
        if (type.isInstance(throwable)) {
            return true;
        }
        return hasCauseOfType(throwable.getCause(), type);
    }
}