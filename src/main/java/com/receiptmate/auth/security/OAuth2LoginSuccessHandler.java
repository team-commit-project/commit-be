package com.receiptmate.auth.security;

import com.receiptmate.user.entity.OAuthProvider;
import com.receiptmate.user.entity.UserCompany;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserCompanyRepository userCompanyRepository;
    private final CookieCsrfTokenRepository csrfTokenRepository;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CsrfToken csrfToken =
            csrfTokenRepository.generateToken(request);

        csrfTokenRepository.saveToken(
            csrfToken,
            request,
            response
        );

        OAuth2AuthenticationToken oauth2Authentication =
        (OAuth2AuthenticationToken) authentication;

        OAuth2User oauth2User = oauth2Authentication.getPrincipal();

        // 어떤 SNS 제공자를 통해 로그인했는지 확인
        String registrationId =
                oauth2Authentication.getAuthorizedClientRegistrationId();

        // 임시: 카카오 사용자 고유 ID (추후 Naver, Google 로그인 설계 시 수정)
        Long kakaoId = oauth2User.getAttribute("id");
        String snsId = kakaoId.toString();

        OAuthProvider oauthProvider =
                OAuthProvider.valueOf(registrationId.toUpperCase());

        log.info("SNS 로그인 성공 - provider={}, snsId={}",
                oauthProvider, snsId);

        String refreshToken = refreshTokenProvider.generateToken();

        refreshTokenService.save(
                refreshToken,
                oauthProvider,
                snsId
        );

        Cookie refreshTokenCookie = new Cookie(
                "refreshToken",
                refreshToken
        );

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14);

        response.addCookie(refreshTokenCookie);

        Optional<UserCompany> user =
                userCompanyRepository.findByOauthProviderAndSnsId(
                        oauthProvider,
                        snsId
                );

        // 1. 신규 회원
        if (user.isEmpty()) {
            log.info("신규 회원입니다. 추가 정보 입력이 필요합니다.");
            response.sendRedirect("/signup");
            return;
        }

        // 2. 기존 회원
        UserCompany existingUser = user.get();

        log.info("기존 회원입니다. userId={}",
                existingUser.getUserId());

        // 3. 기존 회원이지만 추가 정보가 아직 없음
        if (!existingUser.isAdditionalInfoCompleted()) {
            log.info("기존 회원이지만 추가 정보가 미완성입니다.");
            response.sendRedirect("/signup");
            return;
        }

        // 4. 기존 회원 + 추가 정보 완성
        log.info("기존 회원이며 추가 정보가 완성되었습니다.");
        response.sendRedirect("/main");
    }
}