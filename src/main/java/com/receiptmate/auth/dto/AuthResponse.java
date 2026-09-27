package com.receiptmate.auth.dto;

public record AuthResponse(
        String code,
        String message,
        String accessToken,
        Long expiration
) {
}