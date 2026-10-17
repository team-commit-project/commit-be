package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.response.RecentLoginResponse;
import com.receiptmate.auth.entity.LoginHistoryEntity;
import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.repository.LoginHistoryRepository;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginHistoryService {

    private final GeoLocationService geoLocationService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserCompanyRepository userCompanyRepository;

    @Transactional
    public void save(Long userId, String ipAddress) {
        UserCompanyEntity userCompanyEntity = userCompanyRepository
                .findById(userId)
                .orElseThrow(() -> {
                    log.warn("로그인 이력 저장 대상 사용자를 찾을 수 없습니다. userId={}", userId);
                    return new BusinessException((AuthErrorCode.AUTHENTICATION_FAILED));
                });

        String loginRegion = geoLocationService.findRegionOrNull(ipAddress);

        LoginHistoryEntity loginHistoryEntity = new LoginHistoryEntity(userCompanyEntity, ipAddress, loginRegion, LocalDateTime.now());
        loginHistoryRepository.save(loginHistoryEntity);
    }

    public RecentLoginResponse getRecentLogin(Long userId) {
        List<LoginHistoryEntity> loginHistories = loginHistoryRepository.findRecentTwoByUserId(userId);

        // 이전 로그인 이력이 없는 경우
        if (loginHistories.size() < 2) {
            return new RecentLoginResponse(null, null);
        }

        // 현재 로그인 기록을 제외한 이전 로그인 기록
        LoginHistoryEntity previousLogin = loginHistories.get(1);

        return new RecentLoginResponse(previousLogin.getLoginRegion(), previousLogin.getLoginAt());
    }

}
