package com.receiptmate.common.exception;

import com.receiptmate.common.response.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto> handleBusinessException(
            BusinessException e
    ) {

        ErrorCode errorCode = e.getErrorCode();

        ResponseDto response = new ResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;

        ResponseDto response = new ResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e) {

        log.error("예상하지 못한 서버 오류가 발생했습니다.", e);

        ErrorCode errorCode =
                CommonErrorCode.INTERNAL_SERVER_ERROR;

        ResponseDto response = new ResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

}
