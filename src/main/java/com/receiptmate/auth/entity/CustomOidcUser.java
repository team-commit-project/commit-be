package com.receiptmate.auth.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomOidcUser extends DefaultOidcUser implements OAuthPrincipal {

    private final Long userId;
    private final boolean existed;
    private final Map<String, Object> attributes;

    public CustomOidcUser(Long userId, OidcUser oidcUser, boolean existed) {
        super(AuthorityUtils.NO_AUTHORITIES, oidcUser.getIdToken(), "sub");

        this.userId = userId;
        this.existed = existed;

        this.attributes = new HashMap<>(oidcUser.getAttributes());
        this.attributes.put("snsId", oidcUser.getSubject());
        this.attributes.put("joinType", "GOOGLE");
    }

    @Override
    public Map<String, Object> getAttribute(String name) {
        return attributes;
    }
}
