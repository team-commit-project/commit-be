package com.receiptmate.auth.provider;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {

    public ResponseCookie createRefreshTokenCookie(final String refreshToken) {
        return ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public ResponseCookie createSignupTokenCookie(final String signupToken) {
        return ResponseCookie
                .from("signupToken", signupToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(1))
                .build();
    }

}
