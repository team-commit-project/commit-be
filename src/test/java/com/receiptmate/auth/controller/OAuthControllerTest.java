package com.receiptmate.auth.controller;

import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.provider.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("지원하지 않는 SNS 로그인 방식은 400과 에러 정보를 반환")
    public void unsupportedProvider() throws Exception {
        String provider = "instagram";
        mockMvc.perform(get("/api/v1/auth/sns/" + provider))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER.getCode()))
                .andExpect(jsonPath("$.message").value(AuthErrorCode.UNSUPPORTED_SNS_PROVIDER.getMessage()));
    }

}