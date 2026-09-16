package com.receiptmate.auth.exception;

import com.receiptmate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    UNSUPPORTED_SNS_PROVIDER(
            HttpStatus.BAD_REQUEST,
            "UNSUPPORTED_SNS_PROVIDER",
            "지원하지 않는 SNS 로그인 방식입니다."
    ),

    SIGNUP_SESSION_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "SIGNUP_SESSION_EXPIRED",
            "회원가입 인증 정보가 만료되었습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
