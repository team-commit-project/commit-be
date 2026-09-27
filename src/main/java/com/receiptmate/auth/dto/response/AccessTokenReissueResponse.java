package com.receiptmate.auth.dto.response;

import com.receiptmate.common.response.ResponseDto;
import com.receiptmate.common.response.SuccessCode;
import lombok.Getter;

@Getter
public class AccessTokenReissueResponse extends ResponseDto {

    private final String accessToken;
    private final long expiration;

    public AccessTokenReissueResponse(String accessToken, long expiration) {
        super(SuccessCode.SUCCESS.getCode(), SuccessCode.SUCCESS.getMessage());
        this.accessToken = accessToken;
        this.expiration = expiration;
    }
}
