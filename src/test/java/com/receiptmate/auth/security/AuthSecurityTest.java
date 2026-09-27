package com.receiptmate.auth.security;

import com.receiptmate.auth.dto.AccessTokenReissueResult;
import com.receiptmate.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthSecurityTest {

    private static final String REISSUE_URL = "/api/v1/auth/reissue";

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("올바른 CSRF 토큰이면 재발급 요청 허용")
    public void reissueWithValidCsrfToken() throws Exception {
        // given
        String refreshToken = "test-refresh-token";
        String csrfToken = UUID.randomUUID().toString();

        AccessTokenReissueResult result =
                new AccessTokenReissueResult(
                        "new-access-token",
                        300L,
                        "new-refresh-token",
                        Duration.ofHours(1)
                );

        given(authService.reissue(refreshToken)).willReturn(result);

        // when & then
        mockMvc.perform(post(REISSUE_URL)
                    .cookie(
                            new Cookie("refreshToken", refreshToken),
                            new Cookie("csrfToken", csrfToken)
                    )
                    .header("X-CSRF-TOKEN", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"));

        then(authService)
                .should()
                .reissue(refreshToken);
    }

    @Test
    @DisplayName("CSRF 토큰 없이 재발급을 요청하면 403 응답 반환")
    public void reissueWithoutCsrfToken() throws Exception {
        // given
        Cookie refreshToken = new Cookie("refreshToken", "test-refresh-token");

        // when & then
        mockMvc.perform(post(REISSUE_URL)
                    .cookie(refreshToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("CSRF_TOKEN_MISMATCH"))
                .andExpect(jsonPath("$.message")
                        .value("요청이 올바르지 않습니다. 다시 시도해주세요."));
    }

}
