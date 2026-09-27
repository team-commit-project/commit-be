package com.receiptmate.auth.repository;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.exception.RedisOperationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthSignupSessionRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private OAuthSignupSessionRepository repository;

    @Test
    @DisplayName("Redis 조회 중 장애 발생 시 RedisOperationException으로 반환")
    public void findRedisFailure () {
        // given
        String signupToken = "test-signup-token";
        String key = "oauth:signup:" + signupToken;

        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis connection failure");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willThrow(redisException);

        // when & then
        assertThatThrownBy(() -> repository.find(signupToken))
                .isInstanceOf(RedisOperationException.class)
                .hasCause(redisException);
        then(valueOperations).should().get(key);
    }

    @Test
    @DisplayName("Redis 저장 중 장애 발생 시 RedisOperationException으로 반환")
    public void saveRedisFailure() throws JacksonException {
        // given
        String signupToken = "test-signup-token";
        String key = "oauth:signup:" + signupToken;
        String json = "{\"snsId\":\"12345\",\"joinType\":\"KAKAO\"}";

        OAuthSignupSession session = new OAuthSignupSession("12345", "KAKAO");
        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis connection failure");

        given(objectMapper.writeValueAsString(session)).willReturn(json);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        willThrow(redisException)
                .given(valueOperations)
                .set(key, json, Duration.ofMinutes(10));

        // when & then
        assertThatThrownBy(() -> repository.save(signupToken, session))
                .isInstanceOf(RedisOperationException.class)
                .hasCause(redisException);

        then(valueOperations).should().set(key, json, Duration.ofMinutes(10));
    }
}