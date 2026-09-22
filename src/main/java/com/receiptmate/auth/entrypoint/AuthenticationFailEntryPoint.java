package com.receiptmate.auth.entrypoint;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.common.exception.CommonErrorCode;
import com.receiptmate.common.response.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailEntryPoint implements AuthenticationEntryPoint {


    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        log.warn("인증되지 않은 요청이 차단되었습니다. method={}, uri={}, exception={}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getClass().getSimpleName()
        );

        ResponseDto responseDto = new ResponseDto("AUTHENTICATION_FAILED", "로그인이 필요합니다.");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
