package com.receiptmate.auth.handler;

import com.receiptmate.common.exception.CommonErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OAuth2FailureHandlerTest {

    private static final String CLIENT_LOGIN = "httpL//localhost:3000/login";

    private OAuth2FailureHandler oauth2FailureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void before() {
        ObjectMapper objectMapper = new ObjectMapper();

        oauth2FailureHandler = new OAuth2FailureHandler(objectMapper);

        ReflectionTestUtils.setField(oauth2FailureHandler, "oauthClientLogin", CLIENT_LOGIN);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("OAuth 처리 중 DB 오류가 발생하면 500과 에러 정보를 반환")
    public void databaseError() throws Exception {
        // given
        OAuth2AuthenticationException exception =
                new OAuth2AuthenticationException(new OAuth2Error(CommonErrorCode.DATABASE_ERROR.getCode()));

        // when
        oauth2FailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getStatus()).isEqualTo(CommonErrorCode.DATABASE_ERROR.getHttpStatus().value());
        assertThat(response.getContentAsString())
                .contains(CommonErrorCode.DATABASE_ERROR.getCode())
                .contains(CommonErrorCode.DATABASE_ERROR.getMessage());
    }

    @Test
    @DisplayName("OAuth 인증 자체가 실패하면 로그인 페이지로 리다이렉트")
    public void oauthAuthenticationFailure() throws Exception {
        // given
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(new OAuth2Error("oauth_failed"));

        // when
        oauth2FailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(CLIENT_LOGIN + "?error=oauth_failed");
    }
}