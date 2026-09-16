package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import com.receiptmate.auth.entity.CustomOAuth2User;
import com.receiptmate.auth.provider.JwtProvider;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthUserLookupService oAuthUserLookupService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // SNS Provider에서 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // kakao, naver 식별
        String registration = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        // SNS 고유 사용자 ID 추출
        String snsId = getSnsId(oAuth2User, registration);

        OAuthUserResult result;

        try {
            result = oAuthUserLookupService.findUser(registration, snsId, oAuth2User.getAttributes());
        } catch (DataAccessException e) {

            // 실제 DB 처리 과정에서 발생한 오류
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(CommonErrorCode.DATABASE_ERROR.getCode()),
                    "OAuth 사용자 DB 처리 중 오류가 발생했습니다.",
                    e
            );

        } catch (BusinessException e) {

            // userId null 등 애플리케이션 내부 비정상 상태
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(e.getErrorCode().getCode()),
                    e.getErrorCode().getMessage(),
                    e
            );

        }

        return new CustomOAuth2User(result.getUserId(), result.getSnsId(), result.getAttributes(), result.getUserStatus());
    }

    // 결과로 받은 유저 정보에서 registration에 따라 id 값을 추출하는 함수
    private String getSnsId(OAuth2User oAuth2User, String registration) {
        String snsId = null;
        if (registration.equals("KAKAO")) {
            snsId = oAuth2User.getName();
        }
        if (registration.equals("NAVER")) {
            Map<String, String> response =  (Map<String, String>) oAuth2User.getAttributes().get("response");
            snsId = response.get("id");
        }

        return snsId;
    }
}
