package com.receiptmate.auth.repository;

import com.receiptmate.auth.dto.OAuthSignupSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

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

            redisTemplate.opsForValue().set(KEY_PREFIX + signupToken, value, TTL);

        } catch (JacksonException e) {
            throw new IllegalStateException("OAuth 회원가입 세션 저장에 실패했습니다.", e);
        }
    }

    public Optional<OAuthSignupSession> find(String signupToken) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + signupToken);

        if (value == null) {
            return Optional.empty();
        }

        try {
            OAuthSignupSession session =
                    objectMapper.readValue(value, OAuthSignupSession.class);

            return Optional.of(session);

        } catch (JacksonException e) {
            throw new IllegalStateException("OAuth 회원가입 세션 조회에 실패했습니다.", e);
        }
    }

    public void delete(String signupToken) {
        redisTemplate.delete(KEY_PREFIX + signupToken);
    }
}
