package com.receiptmate.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final Duration TTL = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;

    // Refresh Token 해시와 userId 저장
    public void save(String refreshTokenHash, Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + refreshTokenHash, String.valueOf(userId),TTL);
    }

    // Refresh Token으로 사용자 조회
    public Optional<Long> findUserId(String refreshTokenHash) {
        String userId = redisTemplate.opsForValue().get(KEY_PREFIX + refreshTokenHash);

        if (userId == null) {
            return Optional.empty();
        }

        return Optional.of(Long.valueOf(userId));
    }

    // Refresh Token 삭제
    public void delete(String refreshTokenHash) {
        redisTemplate.delete(KEY_PREFIX + refreshTokenHash);
    }

}
