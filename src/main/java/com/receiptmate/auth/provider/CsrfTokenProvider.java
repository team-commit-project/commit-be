package com.receiptmate.auth.provider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsrfTokenProvider {

    private final CsrfTokenRepository csrfTokenRepository;

    public CsrfToken issue(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);

        csrfTokenRepository.saveToken(
                csrfToken,
                request,
                response
        );

        return csrfToken;
    }

}
