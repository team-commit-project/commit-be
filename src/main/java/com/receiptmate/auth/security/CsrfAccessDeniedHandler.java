package com.receiptmate.auth.security;

import com.receiptmate.auth.dto.AuthErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        if ("/api/v1/auth/reissue".equals(request.getRequestURI())) {

            AuthErrorResponse errorResponse =
                    new AuthErrorResponse(
                            "CSRF_TOKEN_MISMATCH",
                            "요청이 올바르지 않습니다. 다시 시도해주세요."
                    );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    """
                    {
                      "code": "CSRF_TOKEN_MISMATCH",
                      "message": "요청이 올바르지 않습니다. 다시 시도해주세요."
                    }
                    """
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN
        );
    }
}