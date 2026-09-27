package com.receiptmate.common.exception;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.exception.RedisOperationException;
import com.receiptmate.common.response.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto> handleBusinessException(
            BusinessException e
    ) {

        ErrorCode errorCode = e.getErrorCode();
        ResponseDto response = new ResponseDto(errorCode.getCode(),errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ResponseDto> handlerMissingRequestCookie(
            MissingRequestCookieException e,
            HttpServletRequest request) {
        log.warn("필수 쿠키 누락 - cookieName={}, method={}, url{}", e.getCookieName(), request.getMethod(), request.getRequestURI());

        if ("refreshToken".equals(e.getCookieName())) {
            AuthErrorCode errorCode = AuthErrorCode.INVALID_REFRESH_TOKEN;
            ResponseDto response = new ResponseDto(errorCode.getCode(),errorCode.getMessage());

            return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
        }

        CommonErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
        ResponseDto response = new ResponseDto(errorCode.getCode(),errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {

        log.warn("요청 값 검증 실패 - method={}, uri={}, errorCount={}",request.getMethod(), request.getRequestURI(), e.getBindingResult().getErrorCount());

        ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
        ResponseDto response = new ResponseDto(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseDto> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {

        log.warn("지원하지 않는 HTTP 메서드 요청 - method={}, uri={}", e.getMethod(), request.getRequestURI());

        CommonErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        ResponseDto response = new ResponseDto(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(RedisOperationException.class)
    public ResponseEntity<ResponseDto> handleRedisOperationException(
            RedisOperationException e,
            HttpServletRequest request)
    {

        AuthErrorCode errorCode = AuthErrorCode.REDIS_ERROR;
        ResponseDto response = new ResponseDto(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e, HttpServletRequest request) {

        log.error("예상하지 못한 서버 오류 - method={}, uri={}", request.getMethod(), request.getRequestURI(), e);

        ErrorCode errorCode =
                CommonErrorCode.INTERNAL_SERVER_ERROR;

        ResponseDto response = new ResponseDto(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

}
