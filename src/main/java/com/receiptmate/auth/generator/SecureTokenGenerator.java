package com.receiptmate.auth.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

// OAuth 추가 회원가입 토큰과 Refresh Token에 사용할 예측 불가능한 보안용 랜덤 토큰 생성
@Component
public class SecureTokenGenerator {
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
