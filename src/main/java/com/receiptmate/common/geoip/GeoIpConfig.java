package com.receiptmate.common.geoip;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
public class GeoIpConfig {

    @Bean(destroyMethod = "close")
    public DatabaseReader databaseReader(@Value("${geoip.database-path}") String databasePath) throws IOException {
        File databaseFile = new File(databasePath);

        if (!databaseFile.isFile() || !databaseFile.canRead()) {
            throw new IllegalStateException("GeoIP 데이터베이스 파일을 읽을 수 없습니다: " + databaseFile.getAbsolutePath());
        }

        return new DatabaseReader.Builder(databaseFile)
                .locales(List.of("en"))
                .withCache(new CHMCache())
                .build();
    }

}
