package com.receiptmate.auth.entity;

import com.receiptmate.user.type.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, OAuthPrincipal {

    private final Long userId;
    private final String snsId;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    // description: 회원가입 여부 //
    private UserStatus userStatus;

    public CustomOAuth2User(Long userId, String snsId, Map<String, Object> attributes, UserStatus userStatus) {
        this.userId = userId;
        this.snsId = snsId;
        this.attributes = attributes;
        this.authorities = AuthorityUtils.NO_AUTHORITIES;
        this.userStatus = userStatus;
    }

    @Override
    public String getName() {
        return snsId;
    }

}
