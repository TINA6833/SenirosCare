package com.ryanshiun.seniorscare.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private final OAuth2User oauth2User;
    // 這是我們最主要的目的：提供一個方法來取得 member_id
    @Getter
    private final Integer memberId;

    public CustomOAuth2User(OAuth2User oauth2User, Integer memberId) {
        this.oauth2User = oauth2User;
        this.memberId = memberId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 直接給予會員固定的角色
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }

    @Override
    public String getName() {
        // Spring Security 預期 getName() 回傳的是 provider 的唯一 ID，我們保持不變
        return oauth2User.getName(); // 這仍然是 lineUserId
    }
}
