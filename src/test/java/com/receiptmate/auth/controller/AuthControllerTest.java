package com.receiptmate.auth.controller;

import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.exception.AuthErrorCode;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                patch("/api/v1/auth/signup-complete")
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
                patch("/api/v1/auth/signup-complete")
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
}