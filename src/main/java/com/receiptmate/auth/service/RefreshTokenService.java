package com.receiptmate.auth.service;

import com.receiptmate.user.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_EXPIRATION =
            Duration.ofDays(14);

    private final StringRedisTemplate redisTemplate;

    public void save(
            String refreshToken,
            OAuthProvider oauthProvider,
            String snsId
    ) {
        String key = "refresh_token:" + refreshToken;

        String value = oauthProvider.name() + ":" + snsId;

        redisTemplate.opsForValue().set(
                key,
                value,
                REFRESH_TOKEN_EXPIRATION
        );
    }
}