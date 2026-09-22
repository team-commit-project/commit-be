package com.receiptmate.auth.provider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthCookieProviderTest {

    private  AuthCookieProvider authCookieProvider;

    @BeforeEach
    void before() {
        authCookieProvider = new AuthCookieProvider();

        // 로컬 환경의 cookie secure 설정
        ReflectionTestUtils.setField(authCookieProvider, "cookieSecure", false);
    }

    @Test
    @DisplayName("Refresh Token 쿠키를 인증 정책에 맞게 생성")
    public void createRefreshTokenCookie() {
        // given
        String refreshToken = "refresh-token";

        // when
        ResponseCookie cookie = authCookieProvider.createRefreshTokenCookie(refreshToken);

        // then
        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    @DisplayName("Signup Token 쿠키를 인증 정책에 맞게 생성")
    public void createSignupTokenCookie() {
        // given
        String signupToken = "signup-token";

        // when
        ResponseCookie cookie = authCookieProvider.createSignupTokenCookie(signupToken);

        // then
        assertThat(cookie.getName()).isEqualTo("signupToken");
        assertThat(cookie.getValue()).isEqualTo(signupToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(10));
    }
}