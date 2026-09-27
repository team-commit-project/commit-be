package com.receiptmate.auth.dto;

public record AuthErrorResponse(
        String code,
        String message
) {
}