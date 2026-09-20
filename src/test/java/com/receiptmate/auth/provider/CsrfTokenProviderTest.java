package com.receiptmate.auth.provider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CsrfTokenProviderTest {

    @Mock
    private CsrfTokenRepository csrfTokenRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private CsrfToken csrfToken;
    private CsrfTokenProvider csrfTokenProvider;

    @BeforeEach
    void before() {
        csrfTokenProvider = new CsrfTokenProvider(csrfTokenRepository);
    }

    @Test
    @DisplayName("CSRF Token을 생성하고 저장한 후 반환")
    public void issueCsrfToken() {
        // given
        given(csrfTokenRepository.generateToken(request)).willReturn(csrfToken);

        // when
        CsrfToken result = csrfTokenProvider.issue(request, response);

        // then
        then(csrfTokenRepository).should().generateToken(request);
        then(csrfTokenRepository).should().saveToken(csrfToken, request, response);
        assertThat(result).isSameAs(csrfToken);
    }

}