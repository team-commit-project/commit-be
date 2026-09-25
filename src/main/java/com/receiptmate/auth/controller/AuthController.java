package com.receiptmate.auth.controller;

import com.receiptmate.auth.dto.AccessTokenReissueResult;
import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.dto.response.AccessTokenReissueResponse;
import com.receiptmate.auth.dto.response.SignupCompleteResponse;
import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.provider.AuthCookieProvider;
import com.receiptmate.auth.provider.CsrfTokenProvider;
import com.receiptmate.auth.service.AuthService;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.type.OAuthProviderType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieProvider authCookieProvider;
    private final CsrfTokenProvider csrfTokenProvider;

    @GetMapping("/sns/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        Optional<OAuthProviderType> matchedProvider = OAuthProviderType.fromRegistrationId(provider);

        OAuthProviderType oauthProvider = matchedProvider.orElseThrow(() ->
                new BusinessException(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER)
        );

        response.sendRedirect("/oauth2/authorization/" + oauthProvider.getRegistrationId());
    }

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

    @PostMapping("/reissue")
    public AccessTokenReissueResponse reissue(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AccessTokenReissueResult result = authService.reissue(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                authCookieProvider.createRefreshTokenCookie(
                        result.getRefreshToken(),
                        result.getRefreshTokenTtl()
                ).toString()
        );

        return new AccessTokenReissueResponse(
                result.getAccessToken(),
                result.getExpiration()
        );
    }
}
