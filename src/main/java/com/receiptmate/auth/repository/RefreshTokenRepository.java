package com.receiptmate.auth.repository;

import com.receiptmate.auth.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final Duration TTL = Duration.ofDays(1);

    private static final long ROTATE_INVALID_TOKEN = -1L;
    private static final long ROTATE_KEY_CONFLICT = -2L;

    /*
        동일 Refresh Token의 중복 재발급을 막기 위해 기존 토큰 검증 -> 남은 TTL 조회 -> 새 토큰 저장 -> 기존 토큰 삭제를 다른 요청이 중간에 끼어들지 않도록 Redis에서 한 번에 처리

        -1: 기존 토큰 없음 / userId 불일치 / 정상적인 TTL 없음
        -2: 새 Refresh Token key가 이미 존재함
        양수: 기존 Refresh Token의 남은 TTL(ms)
    */
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local currentUserId = redis.call('GET', KEYS[1])
            
            if not currentUserId or currentUserId ~= ARGV[1] then
                return -1
            end
    
            local remainingMillis = redis.call('PTTL', KEYS[1])
    
            if remainingMillis <= 0 then
                return -1
            end
    
            if not redis.call(
                'SET',
                KEYS[2],
                currentUserId,
                'PX',
                remainingMillis,
                'NX'
            ) then
                return -2
            end
    
            redis.call('DEL', KEYS[1])
    
            return remainingMillis
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    // Refresh Token 해시를 key, userId를 value로 저장
    public void save(String refreshTokenHash, Long userId) {
        executeRedisCommand(
                () -> redisTemplate.opsForValue().set(KEY_PREFIX + refreshTokenHash, String.valueOf(userId),TTL),
                "Refresh Token 저장"
        );
    }

    // Refresh Token으로 사용자 조회
    public Optional<Long> findUserId(String refreshTokenHash) {

        String userId = executeRedis(
                () -> redisTemplate.opsForValue().get(KEY_PREFIX + refreshTokenHash),
                "Refresh Token 조회"

        );

        if (userId == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            log.error("Redis에 저장된 Refresh Token의 userId 형식 오류", e);
            throw new RedisOperationException("Redis에 저장된 userId 형식이 올바르지 않습니다.", e);
        }
    }

    // 기존 토큰 확인, 남은 TTL 조회, 새 토큰 저장, 기존 토큰 삭제를 Redis에서 원자적으로 수행
    public Optional<Duration> rotate(String oldRefreshTokenHash, String newRefreshTokenHash, Long userId) {

        Long remainingMillis = executeRedis(
                () -> redisTemplate.execute(
                        ROTATE_SCRIPT,
                        List.of(
                                KEY_PREFIX + oldRefreshTokenHash,
                                KEY_PREFIX + newRefreshTokenHash
                        ),
                        String.valueOf(userId)
                ),
                "Refresh Token 교체"
        );

        // Lua Script 결과가 반환되지 않은 경우
        if (remainingMillis == null) {
            log.error("Refresh Token 교체 실패 - Lua Script 실행 결과 없음");
            throw new RedisOperationException("Redis Refresh Token rotation 결과가 반환되지 않았습니다");
        }

        // 기존 Refresh Token이 유효하지 않은 경우
        if (remainingMillis == -1L) {
            return Optional.empty();
        }

        // 새 Refresh Token 키 충돌
        if (remainingMillis == -2L) {
            log.error("Refresh Token 교체 실패 - 새 Refresh Token 키 충돌");
            throw new RedisOperationException("새 Refresh Token 키가 이미 존재합니다.");
        }

        return Optional.of(Duration.ofMillis(remainingMillis));
    }

    // Refresh Token 삭제
    public void delete(String refreshTokenHash) {
        executeRedisCommand(
                () -> redisTemplate.delete(KEY_PREFIX + refreshTokenHash),
                "Refresh Token 삭제"
        );
    }

    // 반환값이 필요한 Redis 작업의 예외 처리
    private <T> T executeRedis(Supplier<T> action, String operation) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            String errorMessage = operation + " 중 Redis 오류 발생";
            log.error(errorMessage, e);
            throw new RedisOperationException(errorMessage, e);
        }
    }

    // 반환값이 필요 없는 Redis 작업의 예외 처리
    private void executeRedisCommand(Runnable action, String operation) {
        try {
            action.run();
        } catch (DataAccessException e) {
            String errorMessage = operation + " 중 Redis 오류 발생";
            log.error(errorMessage, e);
            throw new RedisOperationException(errorMessage, e);
        }
    }

}
