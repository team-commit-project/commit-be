package com.receiptmate.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public class AccessTokenReissueResult {
    private String accessToken;
    private long expiration;
    private String refreshToken;
    private Duration refreshTokenTtl;
}
