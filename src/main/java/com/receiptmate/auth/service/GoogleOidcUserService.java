package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import com.receiptmate.auth.entity.CustomOidcUser;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

        OAuthUserResult result;

        try {
            result = oAuthUserLookupService.findUser(registration, snsId, oidcUser.getAttributes());
        } catch (DataAccessException e) {

            // 실제 DB 처리 과정에서 발생한 오류
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(CommonErrorCode.DATABASE_ERROR.getCode()),
                    "Google OAuth 사용자 DB 처리 중 오류가 발생했습니다.",
                    e
            );

        } catch (BusinessException e) {

            // userId null 등 애플리케이션 내부 비정상 상태
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(e.getErrorCode().getCode()),
                    e.getErrorCode().getMessage(),
                    e
            );

        } catch (RuntimeException e) {
            log.error(
                    "Google OAuth 사용자 처리 중 예상하지 못한 오류가 발생했습니다. provider={}, snsId={}",
                    registration,
                    snsId,
                    e
            );

            throw new OAuth2AuthenticationException(
                    new OAuth2Error(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode()),
                    CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                    e
            );
        }

        return new CustomOidcUser(result.getUserId(), oidcUser, result.getUserStatus());

    }
}
