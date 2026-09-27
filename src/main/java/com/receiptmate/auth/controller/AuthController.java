package com.receiptmate.auth.controller;

import com.receiptmate.auth.dto.AuthErrorResponse;
import com.receiptmate.auth.dto.AuthResponse;
import com.receiptmate.auth.dto.SignupCompleteRequest;
import com.receiptmate.auth.dto.SignupCompleteResponse;
import com.receiptmate.auth.dto.SignupRequest;
import com.receiptmate.auth.service.AuthService;
import com.receiptmate.user.entity.OAuthProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/sns/{provider}")
    public ResponseEntity<Void> snsLogin(@PathVariable String provider) {
        return ResponseEntity
                .status(302)
                .location(URI.create("/oauth2/authorization/" + provider))
                .build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequest request,
            Authentication authentication
    ) {
        OAuth2AuthenticationToken oauth2Authentication =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauth2User =
                oauth2Authentication.getPrincipal();

        String registrationId =
                oauth2Authentication.getAuthorizedClientRegistrationId();

        Long kakaoId = oauth2User.getAttribute("id");
        String snsId = kakaoId.toString();

        OAuthProvider oauthProvider =
                OAuthProvider.valueOf(registrationId.toUpperCase());

        Long userId = authService.signup(
                oauthProvider,
                snsId,
                request
        );

        return ResponseEntity
                .created(URI.create("/api/v1/users/" + userId))
                .build();
    }

        @PatchMapping("/signup-complete")
        public ResponseEntity<SignupCompleteResponse> signupComplete(
                @Valid @RequestBody SignupCompleteRequest request,
                HttpServletRequest httpRequest
        ) {
        Cookie[] cookies = httpRequest.getCookies();

        String signupToken = null;

        if (cookies != null) {
                for (Cookie cookie : cookies) {
                if ("signupToken".equals(cookie.getName())) {
                        signupToken = cookie.getValue();
                        break;
                }
                }
        }

        if (signupToken == null) {
                return ResponseEntity.status(401).build();
        }

        String refreshToken =
                authService.signupComplete(
                        signupToken,
                        request
                );

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(
                        "refreshToken",
                        refreshToken
                )
                .httpOnly(true)
                .path("/api/v1/auth")
                .maxAge(60 * 60 * 24)
                .build();

        ResponseCookie signupTokenCookie =
                ResponseCookie.from(
                        "signupToken",
                        ""
                )
                .httpOnly(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .build();

        SignupCompleteResponse response =
        new SignupCompleteResponse(
                "SIGNUP_COMPLETED",
                "회원가입이 완료되었습니다."
        );

        return ResponseEntity
                .ok()
                .header(
                        "Set-Cookie",
                        refreshTokenCookie.toString()
                )
                .header(
                        "Set-Cookie",
                        signupTokenCookie.toString()
                )
                .body(response);

        }

        @PostMapping("/reissue")
        public ResponseEntity<?> reissue(
                @CookieValue(value = "refreshToken", required = false)
                String refreshToken,

                @CookieValue(value = "csrfToken", required = false)
                String csrfToken,

                @RequestHeader(value = "X-CSRF-TOKEN", required = false)
                String csrfHeader
        ) {
                if (refreshToken == null) {
                        AuthErrorResponse response = new AuthErrorResponse(
                                "REFRESH_TOKEN_NOT_FOUND",
                                "로그인이 만료되었습니다. 다시 로그인해주세요."
                        );

                        return ResponseEntity
                                .status(401)
                                .body(response);
                }

                if (csrfToken == null
                        || csrfHeader == null
                        || !csrfToken.equals(csrfHeader)) {

                        AuthErrorResponse response = new AuthErrorResponse(
                                "CSRF_TOKEN_MISMATCH",
                                "요청이 올바르지 않습니다. 다시 시도해주세요."
                        );

                        return ResponseEntity
                                .status(403)
                                .body(response);
                }

                try {
                        String accessToken =
                                authService.reissueAccessToken(refreshToken);

                        AuthResponse response = new AuthResponse(
                                "SUCCESS",
                                "요청이 성공적으로 처리되었습니다.",
                                accessToken,
                                300L
                        );

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {

                        AuthErrorResponse response = new AuthErrorResponse(
                                "INVALID_REFRESH_TOKEN",
                                "로그인이 만료되었습니다. 다시 로그인해주세요."
                        );

                        return ResponseEntity
                                .status(401)
                                .body(response);
                }
        }
        
}