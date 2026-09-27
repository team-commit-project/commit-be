package com.receiptmate.auth.handler;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.common.response.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        if (exception instanceof CsrfException) {
            log.warn(
                    "CSRF 검증 실패 - method={}, uri={}, reason={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    exception.getClass().getSimpleName()
            );

            AuthErrorCode errorCode = AuthErrorCode.CSRF_TOKEN_MISMATCH;

            ResponseDto responseDto = new ResponseDto(errorCode.getCode(), errorCode.getMessage());

            response.setStatus(errorCode.getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            objectMapper.writeValue(response.getWriter(), responseDto);

            return;
        }

        defaultHandler.handle(request, response, exception);
    }


}
