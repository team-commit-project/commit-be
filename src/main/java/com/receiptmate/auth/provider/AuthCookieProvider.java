package com.receiptmate.auth.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {

    @Value("${security.cookie.secure}")
    private boolean cookieSecure;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken, Duration remainingTtl) {
        return ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(remainingTtl)
                .build();
    }

    public ResponseCookie createSignupTokenCookie(String signupToken) {
        return ResponseCookie
                .from("signupToken", signupToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMinutes(10))
                .build();
    }

    public ResponseCookie createSignupTokenDeletionCookie() {
        return ResponseCookie
                .from("signupToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

}
