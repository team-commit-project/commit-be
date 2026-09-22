package com.receiptmate.auth.service;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.generator.SecureTokenGenerator;
import com.receiptmate.auth.repository.RefreshTokenRepository;
import com.receiptmate.common.exception.BusinessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private SecureTokenGenerator secureTokenGenerator;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void before() {
        refreshTokenService = new RefreshTokenService(secureTokenGenerator, refreshTokenRepository);
    }

    @Test
    @DisplayName("Refresh Token 원문을 생성하고 해시값을 Redis에 저장한 뒤 원문을 반환")
    public void issueRefreshToken() throws Exception {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";
        String expectedHash = sha256(refreshToken);

        given(secureTokenGenerator.generate()).willReturn(refreshToken);

        // when
        String result = refreshTokenService.issue(userId);

        // then
        then(refreshTokenRepository).should().save(expectedHash, userId);
        assertThat(result).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("유효한 Refresh Token이면 사용자 ID를 반환")
    public void validateRefreshToken() throws Exception {
        // given
        String refreshToken = "refresh-token";
        String refreshTokenHash = sha256(refreshToken);
        Long userId = 1L;

        given(refreshTokenRepository.findUserId(refreshTokenHash)).willReturn(Optional.of(userId));

        // when
        Long result = refreshTokenService.validate(refreshToken);

        // then
        assertThat(result).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token이면 인증 예외 발생")
    public void validateInvalidRefreshToken() throws Exception {
        // given
        String refreshToken = "invalid-refresh-token";
        String refreshTokenHash = sha256(refreshToken);

        given(refreshTokenRepository.findUserId(refreshTokenHash))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.validate(refreshToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hashBytes);
    }

}