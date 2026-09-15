package com.receiptmate.auth.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

// OAuth 추가 회원가입 세션의 Redis Key로 사용할 예측 불가능한 랜덤 토큰을 생성
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
