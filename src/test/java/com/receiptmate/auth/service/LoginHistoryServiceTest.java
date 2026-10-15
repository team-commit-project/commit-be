package com.receiptmate.auth.service;

import com.receiptmate.auth.entity.LoginHistoryEntity;
import com.receiptmate.auth.repository.LoginHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
}