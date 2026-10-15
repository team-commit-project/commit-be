package com.receiptmate.auth.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private final DatabaseReader databaseReader;

    public String findRegionOrNull (String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return null;
        }

        try {
            InetAddress address = InetAddress.getByName(ipAddress);

            // 로컬 및 일부 비공개 IP는 지역 조회 제외
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                return null;
            }

            return databaseReader.tryCity(address)
                    .map(response -> Stream.of(
                                    response.city().name(),
                                    response.mostSpecificSubdivision().name(),
                                    response.country().name()
                            )
                            .filter(StringUtils::hasText)
                            .findFirst()
                            .orElse(null))
                    .orElse(null);

        } catch (IOException | GeoIp2Exception exception) {
            log.warn("GeoIP 지역 조회에 실패했습니다.", exception);
            return null;
        }
    }
}
