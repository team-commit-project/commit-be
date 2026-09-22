package com.receiptmate.auth.controller;

import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.dto.response.SignupCompleteResponse;
import com.receiptmate.auth.provider.AuthCookieProvider;
import com.receiptmate.auth.provider.CsrfTokenProvider;
import com.receiptmate.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieProvider authCookieProvider;
    private final CsrfTokenProvider csrfTokenProvider;

    @PatchMapping(value = "/signup-complete")
    public SignupCompleteResponse completeSignup(
            @CookieValue(name = "signupToken", required = false) String signupToken,
            @Valid @RequestBody SignupCompleteRequest signupCompleteRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Long userId = authService.completeSignup(signupToken, signupCompleteRequest);

        // 트랜잭션 프록시가 DB 커밋을 마치고 반환한 뒤 정상 로그인 인증을 발급한다.
        String refreshToken = authService.issueRefreshTokenAfterSignup(signupToken, userId);

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.createRefreshTokenCookie(refreshToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.createSignupTokenDeletionCookie().toString());
        csrfTokenProvider.issue(request, response);

        return new SignupCompleteResponse();
    }
}
