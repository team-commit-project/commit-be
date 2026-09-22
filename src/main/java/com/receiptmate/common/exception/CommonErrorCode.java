package com.receiptmate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "입력값을 다시 확인해주세요."
    ),

    DATABASE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "DATABASE_ERROR",
            "요청을 처리하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "일시적인 서비스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
