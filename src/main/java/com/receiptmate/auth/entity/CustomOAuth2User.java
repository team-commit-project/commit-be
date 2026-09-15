package com.receiptmate.auth.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, OAuthPrincipal {

    private Long userId;
    private String name;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;
    // description: 회원가입 여부 //
    private boolean existed;

    public CustomOAuth2User(Long userId, String name, Map<String, Object> attributes, boolean existed) {
        this.userId = userId;
        this.name = name;
        this.attributes = attributes;
        this.authorities = AuthorityUtils.NO_AUTHORITIES;
        this.existed = existed;
    }

}
