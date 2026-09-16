package com.receiptmate.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class CsrfConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieName("csrfToken");
        repository.setHeaderName("X-CSRF-TOKEN");
        repository.setCookiePath("/");

        return repository;
    }

}
