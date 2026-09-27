package com.receiptmate.auth.service;

import com.receiptmate.user.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SignupTokenService {

    private static final Duration SIGNUP_TOKEN_EXPIRATION =
            Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;

    public void save(
            String signupToken,
            OAuthProvider oauthProvider,
            String snsId
    ) {
        String key = "signup_token:" + signupToken;
        String value = oauthProvider.name() + ":" + snsId;

        redisTemplate.opsForValue().set(
                key,
                value,
                SIGNUP_TOKEN_EXPIRATION
        );
    }

    public String get(String signupToken) {
        String key = "signup_token:" + signupToken;

        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String signupToken) {
        String key = "signup_token:" + signupToken;

        redisTemplate.delete(key);
    }
}