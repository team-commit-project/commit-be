package com.receiptmate.auth.controller;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.type.OAuthProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    @GetMapping("/api/v1/auth/sns/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        Optional<OAuthProvider> matchedProvider = OAuthProvider.fromRegistrationId(provider);

        OAuthProvider oauthProvider = matchedProvider.orElseThrow(() ->
                    new BusinessException(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER)
                );

        response.sendRedirect("/oauth2/authorization/" + oauthProvider.getRegistrationId());
    }

}
