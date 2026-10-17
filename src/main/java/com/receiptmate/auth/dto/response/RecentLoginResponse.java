package com.receiptmate.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.receiptmate.common.response.ResponseDto;
import com.receiptmate.common.response.SuccessCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecentLoginResponse extends ResponseDto {

    private String recentLoginRegion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recentLoginAt;

    public RecentLoginResponse(String recentLoginRegion, LocalDateTime recentLoginAt) {
        super(SuccessCode.SUCCESS.getCode(), SuccessCode.SUCCESS.getMessage());
        this.recentLoginRegion = recentLoginRegion;
        this.recentLoginAt = recentLoginAt;
    }
}
