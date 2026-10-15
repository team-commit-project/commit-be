package com.receiptmate.auth.service;

import com.maxmind.geoip2.DatabaseReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GeoLocationServiceTest {

    @Mock
    private DatabaseReader databaseReader;

    @InjectMocks
    private GeoLocationService geoLocationService;

    @Test
    @DisplayName("")
    public void returnNullWhenGeoIpLookupFails() throws Exception{
        // given
        String ipAddress = "8.8.8.8";

        given(databaseReader.tryCity(any(InetAddress.class)))
                .willThrow(new IOException("GeoIP 조회 실패"));

        // when
        String region = geoLocationService.findRegionOrNull(ipAddress);

        // then
        assertThat(region).isNull();
    }

}