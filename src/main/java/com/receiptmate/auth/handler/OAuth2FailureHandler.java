package com.receiptmate.auth.handler;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.common.exception.CommonErrorCode;
import com.receiptmate.common.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Value("${oauth.client-login}")
    private String oauthClientLogin;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof OAuth2AuthenticationException oauthException) {

            String errorCode = oauthException.getError().getErrorCode();

            // DB 처리 오류
            if (CommonErrorCode.DATABASE_ERROR.getCode().equals(errorCode)) {
                writeError(response, CommonErrorCode.DATABASE_ERROR);
                return;
            }

            // 예기치 못한 서버 내부 오류
            if (CommonErrorCode.INTERNAL_SERVER_ERROR.getCode().equals(errorCode)) {
                writeError(response, CommonErrorCode.INTERNAL_SERVER_ERROR);
                return;
            }

            // 그 외 OAuth 인증 자체 실패
            response.sendRedirect(oauthClientLogin + "?error=oauth_failed");
        }
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "code", errorCode.getCode(),
                        "message", errorCode.getMessage()
                )
        );
    }
}
