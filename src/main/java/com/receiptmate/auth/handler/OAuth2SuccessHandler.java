package com.receiptmate.auth.handler;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.entity.CustomOAuth2User;
import com.receiptmate.auth.entity.OAuthPrincipal;
import com.receiptmate.auth.generator.SecureTokenGenerator;
import com.receiptmate.auth.repository.OAuthSignupSessionRepository;
import com.receiptmate.auth.service.RefreshTokenService;
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
    private String oAuthClientAuth;

    private final SecureTokenGenerator secureTokenGenerator;
    private final OAuthSignupSessionRepository oAuthSignupSessionRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuthPrincipal oAuth2User = (OAuthPrincipal) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        boolean existed = oAuth2User.isExisted();

        // 회원가입 O
        if (existed) {

            Long userId = oAuth2User.getUserId();

            // Refresh Token 발급 + Redis 저장
            String refreshToken = refreshTokenService.issue(userId);

            // Redis Token 원문은 HttpOnly Cookie에 저장
            ResponseCookie refreshTokenCookie = ResponseCookie
                    .from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/api/v1/auth")
                    .maxAge(Duration.ofDays(1))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            response.sendRedirect(oAuthClientMain);
            return;
        }

        // 회원가입 X
        else {
            String snsId = (String) attributes.get("snsId");
            String joinType = (String)  attributes.get("joinType");

            // Redis Key로 사용할 랜덤 signupToken 생성
            String signupToken = secureTokenGenerator.generate();

            // 실제 OAuth 회원가입 정보는 Redis에 저장
            OAuthSignupSession signupSession = new OAuthSignupSession(snsId, joinType);
            oAuthSignupSessionRepository.save(signupToken, signupSession);

            // 브라우저에 signupToken만 쿠키로 전달
            ResponseCookie signupTokenCookie = ResponseCookie
                    .from("signupToken", signupToken)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(10))
                    .build();

            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    signupTokenCookie.toString()
            );

            response.sendRedirect(oAuthClientAuth);
        }
    }
}
