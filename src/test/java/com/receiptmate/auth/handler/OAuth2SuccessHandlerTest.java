package com.receiptmate.auth.handler;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.entity.OAuthPrincipal;
import com.receiptmate.auth.generator.SecureTokenGenerator;
import com.receiptmate.auth.provider.AuthCookieProvider;
import com.receiptmate.auth.provider.CsrfTokenProvider;
import com.receiptmate.auth.repository.OAuthSignupSessionRepository;
import com.receiptmate.auth.service.RefreshTokenService;
import com.receiptmate.user.type.UserStatus;
import jakarta.servlet.ServletException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    private static final String CLIENT_MAIN = "http://localhost:3000/main";
    private static final String CLIENT_SIGNUP = "http://localhost:3000/signup";

    @Mock
    private SecureTokenGenerator secureTokenGenerator;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private OAuthSignupSessionRepository oAuthSignupSessionRepository;
    @Mock
    private AuthCookieProvider authCookieProvider;
    @Mock
    private CsrfTokenProvider csrfTokenProvider;

    @Mock
    private Authentication authentication;
    @Mock
    private OAuthPrincipal oAuthPrincipal;

    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach void before() {

        oAuth2SuccessHandler = new OAuth2SuccessHandler(
                secureTokenGenerator,
                refreshTokenService,
                oAuthSignupSessionRepository,
                authCookieProvider,
                csrfTokenProvider
        );

        ReflectionTestUtils.setField(oAuth2SuccessHandler, "oauthClientMain", CLIENT_MAIN);
        ReflectionTestUtils.setField(oAuth2SuccessHandler, "oauthClientSignup", CLIENT_SIGNUP);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    @Test
    @DisplayName("ACTIVE 사용자는 OAuth 로그인 성공 시 Refresh Token을 발급히고 메인으로 리다이렉트")
    public void activeUserIssueRefreshToken() throws ServletException, IOException {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        givenOAuthUserStatus(UserStatus.ACTIVE);
        given(oAuthPrincipal.getUserId()).willReturn(userId);
        given(refreshTokenService.issue(userId)).willReturn(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/api/v1/auth")
                .build();

        given(authCookieProvider.createRefreshTokenCookie(refreshToken)).willReturn(refreshTokenCookie);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        then(csrfTokenProvider).should().issue(request, response);

        then(refreshTokenService).should().issue(userId);

        then(authCookieProvider).should().createRefreshTokenCookie(refreshToken);

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isEqualTo(refreshTokenCookie.toString());

        assertThat(response.getRedirectedUrl()).isEqualTo(CLIENT_MAIN);
    }

    @Test
    @DisplayName("SIGNUP_REQUIRED 사용자는 signupToken을 발급하고 회원가입 페이지로 리다이렉트")
    public void signupRequiredUserLoginSuccess() throws Exception {
        // given
        String snsId = "1234314";
        String joinType = "KAKAO";
        String signupToken = "signup-token";

        givenOAuthUserStatus(UserStatus.SIGNUP_REQUIRED);
        given(oAuthPrincipal.getAttributes()).willReturn(Map.of("joinType", joinType));
        given(secureTokenGenerator.generate()).willReturn(signupToken);

        ResponseCookie signupTokenCookie = ResponseCookie
                .from("signupToken", signupToken)
                .httpOnly(true)
                .path("/api/v1/auth")
                .build();

        given(authCookieProvider.createSignupTokenCookie(signupToken)).willReturn(signupTokenCookie);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        then(csrfTokenProvider).should().issue(request, response);

        then(secureTokenGenerator).should().generate();

        then(oAuthSignupSessionRepository).should().save(eq(signupToken), any(OAuthSignupSession.class));

        then(authCookieProvider).should().createSignupTokenCookie(signupToken);

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isEqualTo(signupTokenCookie.toString());

        assertThat(response.getRedirectedUrl()).isEqualTo(CLIENT_SIGNUP);
    }

    private void givenOAuthUserStatus(UserStatus userStatus) {
        given(authentication.getPrincipal()).willReturn(oAuthPrincipal);
        given(oAuthPrincipal.getUserStatus()).willReturn(userStatus);
    }

}