package com.receiptmate.auth.repository;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OAuthSignupSessionRepository {

    private static final String KEY_PREFIX = "oauth:signup:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String signupToken, OAuthSignupSession session) {
        try {

            String value = objectMapper.writeValueAsString(session);
            executeRedisCommand(() ->
                    redisTemplate.opsForValue().set(KEY_PREFIX + signupToken, value, TTL),
                    "OAuth 회원가입 세션 저장"
            );

        } catch (JacksonException e) {
            throw new IllegalStateException("OAuth 회원가입 세션 저장에 실패했습니다.", e);
        }
    }

    public Optional<OAuthSignupSession> find(String signupToken) {
        String value = executeRedis(() ->
                redisTemplate.opsForValue().get(KEY_PREFIX + signupToken),
                "OAuth 회원가입 세션 조회"
        );

        if (value == null) {
            return Optional.empty();
        }

        try {

            OAuthSignupSession session = objectMapper.readValue(value, OAuthSignupSession.class);

            if (session == null) {
                throw new IllegalStateException("OAuth 회원가입 세션 역직렬화 결과가 null입니다.");
            }

            return Optional.of(session);

        } catch (JacksonException e) {
            throw new IllegalStateException("OAuth 회원가입 세션 역직렬화에 실패했습니다.", e);
        }
    }

    public void delete(String signupToken) {
        executeRedisCommand(() ->
                redisTemplate.delete(KEY_PREFIX + signupToken),
                "OAuth 회원가입 세션 삭제"
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
