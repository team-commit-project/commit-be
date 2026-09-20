package com.receiptmate.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
    this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
  }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository
        ) throws Exception {
        http
            .csrf(csrf -> {
                CsrfTokenRequestAttributeHandler requestHandler =
                        new CsrfTokenRequestAttributeHandler();

                csrf
                    .csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler((request, response, deferredCsrfToken) -> {
                        requestHandler.handle(request, response, deferredCsrfToken);
                        deferredCsrfToken.get();
                    });
            })
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/authgits/sns/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
            );

        return http.build();
    }
}
