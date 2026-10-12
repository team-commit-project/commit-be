package com.receiptmate.common.geoip;

import com.maxmind.geoip2.DatabaseReader;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.internal.database.DatabaseType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class GeoIpConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withUserConfiguration(GeoIpConfig.class);
    
    @Test
    @DisplayName("GeoLite2 City 데이터베이스 파일을 정상적으로 로드")
    public void registerDatabaseReader() {
        // given
        String databasePath = System.getenv("GEOIP_DATABASE_PATH");
        assertThat(databasePath).isNotBlank();

        // when
        contextRunner
                .withPropertyValues("geoip.dtabase-path=" + databasePath)
                .run(context -> {
                    // then
                    assertThat(context).hasNotFailed();

                    DatabaseReader reader = context.getBean(DatabaseReader.class);
                    assertThat(reader.metadata().databaseType()).isEqualTo("GeoLite2-City");
                });

    }

}