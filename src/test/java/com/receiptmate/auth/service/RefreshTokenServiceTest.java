package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.RotatedRefreshToken;
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
import java.time.Duration;
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

    @Test
    @DisplayName("Refresh Token 교체에 성공하면 새 토큰과 기존 토큰의 남은 TTL을 반환")
    public void rotateRefreshToken() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        Long userId = 1L;
        Duration remainingTtl = Duration.ofHours(12);

        String expectedOldHash = sha256(oldRefreshToken);
        String expectedNewHash = sha256(newRefreshToken);

        given(secureTokenGenerator.generate()).willReturn(newRefreshToken);

        given(refreshTokenRepository.rotate(
                expectedOldHash,
                expectedNewHash,
                userId
        )).willReturn(Optional.of(remainingTtl));

        // when
        RotatedRefreshToken result = refreshTokenService.rotate(oldRefreshToken, userId);

         // then
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(result.getRemainingTtl()).isEqualTo(remainingTtl);
        then(secureTokenGenerator).should().generate();
        then(refreshTokenRepository).should().rotate(expectedOldHash, expectedNewHash, userId);
    }

    @Test
    @DisplayName("기존 Refresh Token이 유효하지 않으면 교체를 거부")
    public void rotateRefreshTokenWithInvalidToken() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        Long userId = 1L;

        String expectedOldHash = sha256(oldRefreshToken);
        String expectedNewHash = sha256(newRefreshToken);

        given(secureTokenGenerator.generate()).willReturn(newRefreshToken);
        given(refreshTokenRepository.rotate(
                expectedOldHash,
                expectedNewHash,
                userId
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.rotate(oldRefreshToken, userId))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception ->
                    assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN)
        );

        then(secureTokenGenerator).should().generate();
        then(refreshTokenRepository).should().rotate(expectedOldHash, expectedNewHash, userId);
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hashBytes);
    }
}