package com.receiptmate.auth.entity;

import com.receiptmate.user.type.UserStatus;
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
    private final String snsId;
    private final UserStatus userStatus;
    private final Map<String, Object> attributes;

    public CustomOidcUser(Long userId, OidcUser oidcUser, UserStatus userStatus) {
        super(AuthorityUtils.NO_AUTHORITIES, oidcUser.getIdToken(), "sub");

        this.userId = userId;
        this.snsId = oidcUser.getSubject();
        this.userStatus = userStatus;

        this.attributes = new HashMap<>(oidcUser.getAttributes());
        this.attributes.put("snsId", oidcUser.getSubject());
        this.attributes.put("joinType", "GOOGLE");
    }

    @Override
    public Map<String, Object> getAttribute(String name) {
        return attributes;
    }
}
