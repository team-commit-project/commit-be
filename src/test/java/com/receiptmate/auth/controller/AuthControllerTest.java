package com.receiptmate.auth.controller;

import com.receiptmate.auth.dto.AccessTokenReissueResult;
import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.exception.RedisOperationException;
import com.receiptmate.auth.provider.AuthCookieProvider;
import com.receiptmate.auth.provider.CsrfTokenProvider;
import com.receiptmate.auth.provider.JwtProvider;
import com.receiptmate.auth.service.AuthService;
import com.receiptmate.common.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private AuthCookieProvider authCookieProvider;
    @MockitoBean
    private CsrfTokenProvider csrfTokenProvider;
    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String OAUTH_LOGIN_URL = "/api/v1/auth/sns/";
    private static final String SIGNUP_COMPLETE_URL = "/api/v1/auth/signup-complete";
    private static final String REISSUE_URL = "/api/v1/auth/reissue";

    @Test
    @DisplayName("지원하지 않는 SNS 로그인 방식은 400과 에러 정보를 반환")
    public void unsupportedProvider() throws Exception {
        String provider = "instagram";
        mockMvc.perform(get(OAUTH_LOGIN_URL + provider))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER.getCode()))
                .andExpect(jsonPath("$.message").value(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER.getMessage()));
    }

    @Test
    @DisplayName("추가 회원가입을 완료하면 Refresh Token 쿠키를 발급하고 signupToken 쿠키를 삭제하며 CSRF Token을 새로 발급")
    public void completesSignup() throws Exception {
        // given
        String signupToken = "signup-token";
        Long userId = 1L;
        String refreshToken = "refresh-token";

        SignupCompleteRequest request = new SignupCompleteRequest(
                "테스트 상사",
                "123-45-67890",
                "온라인 판매업",
                "01012341234",
                1_000_000,
                LocalDate.of(2026, 9, 1)
        );

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .path("/api/v1/auth")
                .httpOnly(true)
                .build();

        ResponseCookie signupTokenDeletionCookie = ResponseCookie
                .from("signupToken", "")
                .path("/api/v1/auth")
                .maxAge(0)
                .httpOnly(true)
                .build();

        given(authService.completeSignup(eq(signupToken), any(SignupCompleteRequest.class))).willReturn(userId);
        given(authService.issueRefreshTokenAfterSignup(signupToken, userId)).willReturn(refreshToken);
        given(authCookieProvider.createRefreshTokenCookie(refreshToken)).willReturn(refreshTokenCookie);
        given(authCookieProvider.createSignupTokenDeletionCookie()).willReturn(signupTokenDeletionCookie);

        // when
        ResultActions result = mockMvc.perform(
                patch(SIGNUP_COMPLETE_URL)
                        .cookie(new Cookie("signupToken", signupToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isOk());

        List<String> setCookies = result.andReturn()
                .getResponse()
                .getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookies)
                .anySatisfy(cookie ->
                    assertThat(cookie)
                            .contains("refreshToken=refresh-token")
                );

        assertThat(setCookies)
                .anySatisfy(cookie ->
                        assertThat(cookie)
                                .contains("signupToken=")
                                .contains("Max-Age=0")

                );

        then(authService).should().completeSignup(
                eq(signupToken), any(SignupCompleteRequest.class)
        );

        then(authService).should().issueRefreshTokenAfterSignup(signupToken, userId);

        then(authCookieProvider).should().createRefreshTokenCookie(refreshToken);

        then(authCookieProvider).should().createSignupTokenDeletionCookie();

        then(csrfTokenProvider).should().issue(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("추가 회원가입 처리에 실패하면 Refresh Token 발급과 쿠키 및 CSRF Token 처리를 수행하지 않음")
    public void completeSignupFail() throws Exception {
        // given
        String signupToken = "signup-token";

        SignupCompleteRequest request = new SignupCompleteRequest(
                "테스트 상사",
                "123-45-67890",
                "온라인 판매업",
                "01012341234",
                1_000_000,
                LocalDate.of(2026, 9, 1)
        );

        given(authService.completeSignup(
                eq(signupToken),
                any(SignupCompleteRequest.class)
        )).willThrow(new BusinessException(AuthErrorCode.ALREADY_SIGNUP_COMPLETED));
        
        // when
        mockMvc.perform(
                patch(SIGNUP_COMPLETE_URL)
                        .cookie(new Cookie("signupToken", signupToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );


        // then
        then(authService).should().completeSignup(
                eq(signupToken),
                any(SignupCompleteRequest.class)
        );

        then(authService).should(never()).issueRefreshTokenAfterSignup(anyString(), anyLong());
        then(authCookieProvider).shouldHaveNoInteractions();
        then(csrfTokenProvider).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Access Token 재발급에 성공하면 Access Token과 새 Refresh Token 쿠키 반환")
    public void reissueAccessToken() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String accessToken = "new-access-token";

        Duration remainingTtl = Duration.ofHours(24);

        AccessTokenReissueResult result = new AccessTokenReissueResult(
                accessToken,
                300L,
                newRefreshToken,
                remainingTtl
        );

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .path("/api/v1/auth")
                .maxAge(remainingTtl)
                .build();

        given(authService.reissue(oldRefreshToken)).willReturn(result);
        given(authCookieProvider.createRefreshTokenCookie(newRefreshToken, remainingTtl)).willReturn(refreshTokenCookie);

        // when & then
        mockMvc.perform(post(REISSUE_URL)
                    .cookie(new Cookie("refreshToken", oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.expiration").value(300))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("refreshToken=" +  newRefreshToken)
        ));

        then(authService).should().reissue(oldRefreshToken);
        then(authCookieProvider).should().createRefreshTokenCookie(newRefreshToken, remainingTtl);
    }
    
    @Test
    @DisplayName("유효하지 않은 Refresh Token이면 401 응답을 반환")
    public void reissueWithInvalidRefreshToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid-refresh-token";

        given(authService.reissue(invalidRefreshToken)).willThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        
        // when & then
        mockMvc.perform(post(REISSUE_URL)
                    .cookie(new Cookie("refreshToken", invalidRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("로그인이 만료되었습니다. 다시 로그인해주세요."));

        then(authService).should().reissue(invalidRefreshToken);
    }

    @Test
    @DisplayName("Refresh Token 쿠키가 없으면 401 응답 반환")
    public void reissueWithoutRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post(REISSUE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message")
                        .value("로그인이 만료되었습니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("Redis 장애 발생 시 500 응답 반환")
    public void reissueWithRedisError() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        given(authService.reissue(refreshToken)).willThrow(new RedisOperationException("Redis 연결 오류"));

        // when & then
        mockMvc.perform(post(REISSUE_URL)
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("REDIS_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("요청을 처리하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요."));

        then(authService).should().reissue(refreshToken);
    }
}