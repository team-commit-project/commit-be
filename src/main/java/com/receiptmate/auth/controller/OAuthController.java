package com.receiptmate.auth.controller;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.type.OAuthProviderType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/sns")
public class OAuthController {

    @GetMapping("/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        Optional<OAuthProviderType> matchedProvider = OAuthProviderType.fromRegistrationId(provider);

        OAuthProviderType oauthProvider = matchedProvider.orElseThrow(() ->
                    new BusinessException(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER)
                );

        response.sendRedirect("/oauth2/authorization/" + oauthProvider.getRegistrationId());
    }

}
