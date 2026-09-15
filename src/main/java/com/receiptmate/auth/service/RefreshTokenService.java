package com.receiptmate.auth.service;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.generator.SecureTokenGenerator;
import com.receiptmate.auth.repository.RefreshTokenRepository;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.common.exception.CommonErrorCode;
import com.receiptmate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final SecureTokenGenerator secureTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public String issue(Long userId) {
        // Refresh Token 원문 생성
        String refreshToken = secureTokenGenerator.generate();

        // Redis에는 원문이 아닌 SHA-256 해시값으로 저장
        String refreshTokenHash = hash(refreshToken);

        refreshTokenRepository.save(refreshTokenHash, userId);

        return refreshToken;
    }

    public Long validate(String refreshToken) {
        String refreshTokenHash = hash(refreshToken);

        Optional<Long> userId = refreshTokenRepository.findUserId(refreshTokenHash);

        if (userId.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return userId.get();
    }

    private String hash(String refreshToken) {
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 해시 처리에 실패했습니다.", e);
        }
    }

}
