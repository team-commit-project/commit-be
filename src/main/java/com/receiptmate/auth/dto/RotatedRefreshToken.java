package com.receiptmate.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public class RotatedRefreshToken {

    private final String refreshToken;
    private final Duration remainingTtl;
}
