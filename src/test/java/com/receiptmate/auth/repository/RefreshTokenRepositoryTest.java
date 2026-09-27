package com.receiptmate.auth.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String oldKey;
    private String newKey;
    private String secondNewKey;

    @AfterEach
    void cleanup() {
        if (oldKey != null) {
            redisTemplate.delete(oldKey);
        }

        if (newKey != null) {
            redisTemplate.delete(newKey);
        }

        if (secondNewKey != null) {
            redisTemplate.delete(secondNewKey);
        }
    }

    @Test
    @DisplayName("Refresh Token 교체 시 기존 토큰을 삭제하고 새 토큰을 저장")
    public void rotateRefreshToken() {
        // given
        Long userId = 1L;

        String oldHash = UUID.randomUUID().toString();
        String newHash = UUID.randomUUID().toString();

        oldKey = "auth:refresh:" + oldHash;
        newKey = "auth:refresh:" + newHash;

        refreshTokenRepository.save(oldHash, userId);

        // when
        Optional<Duration> result = refreshTokenRepository.rotate(oldHash, newHash, userId);

        // then
        assertThat(result).isPresent();
        assertThat(redisTemplate.hasKey(oldKey)).isFalse();
        assertThat(redisTemplate.opsForValue().get(newKey)).isEqualTo(String.valueOf(userId));
    }

    @Test
    @DisplayName("Refresh Token 교체 시 기존 토큰의 남은 TTL을 유지")
    public void rotateRefreshTokenPreservesTtl() {
        // given
        Long userId = 1L;

        String oldHash = UUID.randomUUID().toString();
        String newHash = UUID.randomUUID().toString();

        oldKey = "auth:refresh:" + oldHash;
        newKey = "auth:refresh:" + newHash;

        refreshTokenRepository.save(oldHash, userId);

        Boolean expirationSet = redisTemplate.expire(oldKey, Duration.ofMinutes(30));
        assertThat(expirationSet).isTrue();

        Long remainingMillis = redisTemplate.getExpire(oldKey, TimeUnit.MILLISECONDS);
        assertThat(remainingMillis).isNotNull().isPositive();

        Duration originalTtl = Duration.ofMillis(remainingMillis);

        // when
        Optional<Duration> result = refreshTokenRepository.rotate(oldHash, newHash, userId);

        // then
        assertThat(result).isPresent();
        Duration rotatedTtl = result.orElseThrow();
        assertThat(rotatedTtl).isBetween(originalTtl.minusSeconds(5), originalTtl);

        Long newRemainingMillis = redisTemplate.getExpire(newKey, TimeUnit.MILLISECONDS);
        assertThat(newRemainingMillis).isNotNull().isPositive();
        Duration newTtl = Duration.ofMillis(newRemainingMillis);
        assertThat(newTtl).isBetween(originalTtl.minusSeconds(5), rotatedTtl);
    }

    @Test
    @DisplayName("이미 교체된 Refresh Token으로 다시 요청하면 교체를 거부")
    public void rotateRefreshTokenRejectsReuse() {
        // given
        Long userId = 1L;

        String oldHash = UUID.randomUUID().toString();
        String firstNewHash = UUID.randomUUID().toString();
        String secondNewHash = UUID.randomUUID().toString();

        oldKey = "auth:refresh:" + oldHash;
        newKey = "auth:refresh:" + firstNewHash;
        secondNewKey =  "auth:refresh:" + secondNewHash;

        refreshTokenRepository.save(oldHash, userId);

        // when
        Optional<Duration> firstResult = refreshTokenRepository.rotate(oldHash, firstNewHash, userId);
        Optional<Duration> secondResult = refreshTokenRepository.rotate(oldHash, secondNewHash, userId);

        // then
        assertThat(firstResult).isPresent();
        assertThat(secondResult).isEmpty();

        assertThat(redisTemplate.hasKey(oldKey)).isFalse();
        assertThat(redisTemplate.opsForValue().get(newKey)).isEqualTo(String.valueOf(userId));
        assertThat(redisTemplate.hasKey(secondNewKey)).isFalse();
    }

    @Test
    @DisplayName("동일한 Refresh Token으로 동시에 교체하면 한 요청만 성공")
    public void rotateRefreshTokenConcurrently() throws Exception {
        // given
        Long userId = 1L;

        String oldHash = UUID.randomUUID().toString();
        String firstNewHash = UUID.randomUUID().toString();
        String secondNewHash = UUID.randomUUID().toString();

        oldKey = "auth:refresh:" + oldHash;
        newKey = "auth:refresh:" + firstNewHash;
        secondNewKey = "auth:refresh:" + secondNewHash;

        refreshTokenRepository.save(oldHash, userId);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Optional<Duration>> firstFuture =
                executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return refreshTokenRepository.rotate(oldHash, firstNewHash, userId);
            });

            Future<Optional<Duration>> secondFuture =
                    executor.submit(() -> {
                        ready.countDown();
                        start.await();

                        return refreshTokenRepository.rotate(oldHash, secondNewHash, userId);
            });

            // 두 스레드가 준비될 때까지 대기
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            // 두 스레드에 동시에 출반 신호 전달
            start.countDown();

            Optional<Duration> firstResult = firstFuture.get(10, TimeUnit.SECONDS);
            Optional<Duration> secondResult = secondFuture.get(10, TimeUnit.SECONDS);

            // then
            long successCount = Stream.of(firstResult, secondResult)
                    .filter(Optional::isPresent)
                    .count();
            assertThat(successCount).isEqualTo(1);

            assertThat(redisTemplate.hasKey(oldKey)).isFalse();

            String successKey = firstResult.isPresent() ? newKey : secondNewKey;
            String failedKey = firstResult.isPresent() ? secondNewKey : newKey;

            assertThat(redisTemplate.opsForValue().get(successKey)).isEqualTo(String.valueOf(userId));
            assertThat(redisTemplate.hasKey(failedKey)).isFalse();
        } finally {
            start.countDown();
            executor.shutdown();
        }
    }
}