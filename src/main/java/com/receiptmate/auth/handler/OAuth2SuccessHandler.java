package com.receiptmate.auth.handler;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.entity.CustomOAuth2User;
import com.receiptmate.auth.entity.OAuthPrincipal;
import com.receiptmate.auth.generator.SecureTokenGenerator;
import com.receiptmate.auth.provider.AuthCookieProvider;
import com.receiptmate.auth.provider.CsrfTokenProvider;
import com.receiptmate.auth.repository.OAuthSignupSessionRepository;
import com.receiptmate.auth.service.RefreshTokenService;
import com.receiptmate.user.type.UserStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${oauth.client-main}")
    private String oAuthClientMain;
    @Value("${oauth.client-signup}")
    private String oauthClientSignup;

    private final SecureTokenGenerator secureTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final OAuthSignupSessionRepository oAuthSignupSessionRepository;
    private final AuthCookieProvider authCookieProvider;
    private final CsrfTokenProvider csrfTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuthPrincipal oAuth2User = (OAuthPrincipal) authentication.getPrincipal();

        Long userId = oAuth2User.getUserId();
        String snsId = oAuth2User.getSnsId();
        UserStatus userStatus = oAuth2User.getUserStatus();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // OAuth 인증 성공 후 새로운 CSRF Token 발급
        csrfTokenProvider.issue(request, response);

        // 회원가입 O
        if (userStatus == UserStatus.ACTIVE) {

            // Refresh Token 발급 + Redis 저장
            String refreshToken = refreshTokenService.issue(userId);

            // Redis Token 원문은 HttpOnly Cookie에 저장
            ResponseCookie refreshTokenCookie = authCookieProvider.createRefreshTokenCookie(refreshToken);

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            response.sendRedirect(oAuthClientMain);
            return;
        }

        // 추가 회원가입이 필요한 사용자
        if (userStatus == UserStatus.SIGNUP_REQUIRED) {
            String joinType = (String)  attributes.get("joinType");

            // Redis Key로 사용할 랜덤 signupToken 생성
            String signupToken = secureTokenGenerator.generate();

            // OAuth 회원가입 정보는 Redis에 저장
            OAuthSignupSession signupSession = new OAuthSignupSession(snsId, joinType);
            oAuthSignupSessionRepository.save(signupToken, signupSession);

            // 브라우저에 signupToken만 HttpOnly Cookie로 전달
            ResponseCookie signupTokenCookie = authCookieProvider.createSignupTokenCookie(signupToken);

            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    signupTokenCookie.toString()
            );

            response.sendRedirect(oauthClientSignup);
        }
    }
}
