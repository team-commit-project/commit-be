package com.receiptmate.auth.dto.response;

import com.receiptmate.common.response.ResponseDto;

public class SignupCompleteResponse extends ResponseDto {

    public SignupCompleteResponse() {
        super("SIGNUP_COMPLETED", "회원가입이 완료되었습니다.");
    }
}
