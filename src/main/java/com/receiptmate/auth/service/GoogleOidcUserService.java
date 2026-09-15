package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import com.receiptmate.auth.entity.CustomOidcUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOidcUserService extends OidcUserService {

    private final OAuthUserLookupService oAuthUserLookupService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // Google에서 사용자 정보 조회
        OidcUser oidcUser = super.loadUser(userRequest);

        // GOOGLE 식별
        String registration = userRequest
                .getClientRegistration()
                .getRegistrationId()
                .toUpperCase();

        // Google 고유 사용자 ID
        String snsId = oidcUser.getSubject();

        log.info("registration = {}", registration);
        log.info("snsId = {}", snsId);

        // 공통 회원 조회
        OAuthUserResult result = oAuthUserLookupService.findUser(registration, snsId, oidcUser.getAttributes());

        return new CustomOidcUser(result.getUserId(), oidcUser, result.isExisted());

    }
}
