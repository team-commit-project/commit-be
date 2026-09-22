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

    INVALID_SIGNUP_INFO(
            HttpStatus.BAD_REQUEST,
            "INVALID_SIGNUP_INFO",
            "입력한 회원정보를 다시 확인해주세요."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "로그인이 만료되었습니다. 다시 로그인해주세요."

    ),

    SIGNUP_SESSION_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "SIGNUP_SESSION_EXPIRED",
            "회원가입 인증 정보가 만료되었습니다."
    ),

    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "로그인이 필요합니다."
    ),

    ALREADY_SIGNUP_COMPLETED(
            HttpStatus.CONFLICT,
            "ALREADY_SIGNUP_COMPLETED",
                    "이미 회원가입이 완료된 사용자입니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
