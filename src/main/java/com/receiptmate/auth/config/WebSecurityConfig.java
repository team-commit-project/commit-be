package com.receiptmate.auth.config;

import com.receiptmate.auth.entrypoint.AuthenticationFailEntryPoint;
import com.receiptmate.auth.filter.JwtAuthenticationFilter;
import com.receiptmate.auth.handler.OAuth2FailureHandler;
import com.receiptmate.auth.handler.OAuth2SuccessHandler;
import com.receiptmate.auth.service.GoogleOidcUserService;
import com.receiptmate.auth.service.OAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final GoogleOidcUserService googleOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final AuthenticationFailEntryPoint authenticationFailEntryPoint;
    private final CsrfTokenRepository csrfTokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        PathPatternRequestMatcher.Builder matcher = PathPatternRequestMatcher.withDefaults();

        // Refresh Token / Signup Token처럼 HttpOnly Cookie를 인증 수단으로 사용하는 요청만 CSRF 보호
        RequestMatcher csrfProtectedRequests = new OrRequestMatcher(
                matcher.matcher(HttpMethod.POST, "/api/v1/auth/reissue"),
                matcher.matcher(HttpMethod.POST, "/api/v1/auth/logout"),
                matcher.matcher(HttpMethod.PATCH, "/api/v1/auth/signup-complete")
        );

        security
                // HTTP Basic 인증 사용 X
                .httpBasic(HttpBasicConfigurer::disable)

                // 로그인 상태를 HttpSession에 저장하지 않음
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CSRF
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(csrfTokenRepository)
                        .requireCsrfProtectionMatcher(csrfProtectedRequests)
                )

                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 요청 접근 권한
                .authorizeHttpRequests(request -> request
                        // OAuth 로그인 시작
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // OAuth Provider call back
                        .requestMatchers("/login/oauth2/code/**").permitAll()

                        .anyRequest().authenticated()
                )

                // OAuth2 로그인
                .oauth2Login(oauth2 -> oauth2

                        // Provider 사용자 정보 조회 후 CustomOAuth2User 생성
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                                .oidcUserService(googleOidcUserService)
                        )

                        // 로그인 성공 후 처리
                        .successHandler(oAuth2SuccessHandler)
                        // 로그인 실패 후 처리
                        .failureHandler(oAuth2FailureHandler)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationFailEntryPoint)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:3000")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
