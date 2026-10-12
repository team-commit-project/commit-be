package com.receiptmate.common.geoip;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geoip")
@RequiredArgsConstructor
@Getter
public class GeoIpProperties {

    private final String databasePath;
}
