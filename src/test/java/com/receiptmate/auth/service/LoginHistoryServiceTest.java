package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.response.RecentLoginResponse;
import com.receiptmate.auth.entity.LoginHistoryEntity;
import com.receiptmate.auth.repository.LoginHistoryRepository;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.user.type.OAuthProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class LoginHistoryServiceTest {

    @Autowired
    private LoginHistoryService loginHistoryService;
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;
    @Autowired
    private GeoLocationService geoLocationService;
    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Test
    @DisplayName("공인 IP로 로그인하면 지역 정보가 조회되어 DB에 저장")
    public void saveLoginHistoryWithRegion() {
        // given
        Long userId = 1L;
        String ipAddress = "222.112.54.61";

        // 실제 GoLite2 DB에서 지역 조회
        String expectedRegion = geoLocationService.findRegionOrNull(ipAddress);
        log.info("조회된 지역: {}", expectedRegion);

        // when
        loginHistoryService.save(userId, ipAddress);

        // then
        LoginHistoryEntity loginHistoryEntity = loginHistoryRepository.findRecentTwoByUserId(userId).get(0);

        assertThat(loginHistoryEntity.getIpAddress()).isEqualTo(ipAddress);
        assertThat(loginHistoryEntity.getLoginRegion()).isEqualTo(expectedRegion);
        assertThat(loginHistoryEntity.getLoginAt()).isNotNull();

    }

    @Test
    @DisplayName("현재 로그인을 제외한 이전 로그인 이력의 지역과 일시를 반환")
    public void getRecentLogin() {
        // given
        UserCompanyEntity userCompanyEntity = userCompanyRepository.save(
                UserCompanyEntity.createOAuthUser(UUID.randomUUID().toString(), OAuthProviderType.KAKAO)
        );
        Long userId = userCompanyEntity.getUserId();
        LocalDateTime previousLoginAt =
                LocalDateTime.of(2026, 10, 16, 10, 0);

        loginHistoryRepository.saveAll(List.of(
                new LoginHistoryEntity(userCompanyEntity, "127.0.0.1", "Busan", previousLoginAt.minusDays(1)),
                new LoginHistoryEntity(userCompanyEntity, "127.0.0.1", "Seoul", previousLoginAt),
                new LoginHistoryEntity(userCompanyEntity, "127.0.0.1", "Daejeon", previousLoginAt.plusDays(1))
        ));

        // when
        RecentLoginResponse result = loginHistoryService.getRecentLogin(userId);

        // then
        assertThat(result.getRecentLoginRegion()).isEqualTo("Seoul");
        assertThat(result.getRecentLoginAt()).isEqualTo(LocalDateTime.of(2026, 10, 16, 10, 0));
    }

    @Test
    @DisplayName("이전 로그인 기록이 없으면 지역과 일시를 null로 반환한다")
    void getRecentLoginWithoutPreviousHistory() {

        // given
        UserCompanyEntity userCompanyEntity = userCompanyRepository.save(
                UserCompanyEntity.createOAuthUser(UUID.randomUUID().toString(), OAuthProviderType.KAKAO)
        );
        Long userId = userCompanyEntity.getUserId();

        LocalDateTime currentLoginAt =
                LocalDateTime.of(2026, 10, 17, 10, 0);

        loginHistoryRepository.saveAll(List.of(
                new LoginHistoryEntity(userCompanyEntity, "127.0.0.1", "Seoul", currentLoginAt)
        ));

        // when
        RecentLoginResponse result = loginHistoryService.getRecentLogin(userId);

        // then
        assertThat(result.getRecentLoginRegion()).isNull();
        assertThat(result.getRecentLoginAt()).isNull();
    }
}
