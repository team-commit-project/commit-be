package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.AccessTokenReissueResult;
import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.dto.RotatedRefreshToken;
import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.provider.JwtProvider;
import com.receiptmate.auth.repository.OAuthSignupSessionRepository;
import com.receiptmate.category.entity.CategoryEntity;
import com.receiptmate.category.repository.CategoryRepository;
import com.receiptmate.category.type.DefaultCategory;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.user.type.OAuthProviderType;
import com.receiptmate.user.type.UserStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCompanyRepository userCompanyRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OAuthSignupSessionRepository oAuthSignupSessionRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("추가 회원가입을 완료하면 사용자 정보가 반영되고 ACTIVE 상태로 변경")
    public void completeSignup() {
        // given
        String signupToken = "signup-token";
        Long userId = 1L;

        OAuthSignupSession session = new OAuthSignupSession("sns-id", OAuthProviderType.KAKAO.name());

        SignupCompleteRequest request = new SignupCompleteRequest(
                "테스트 상사",
                "123-45-67890",
                "온라인 판매업",
                "01012341234",
                1_000_000,
                LocalDate.of(2026, 9, 1)
        );

        UserCompanyEntity user = UserCompanyEntity.createOAuthUser("sns-id", OAuthProviderType.KAKAO);
        ReflectionTestUtils.setField(user, "userId", userId);

        given(oAuthSignupSessionRepository.find(signupToken)).willReturn(Optional.of(session));
        given(userCompanyRepository.findForSignupByOauthProviderAndSnsId(OAuthProviderType.KAKAO, "sns-id"))
                .willReturn(Optional.of(user));

        // when
        Long result = authService.completeSignup(signupToken, request);

        // then
        assertThat(result).isEqualTo(userId);

        assertThat(user.getCompanyName()).isEqualTo("테스트 상사");
        assertThat(user.getBusinessNumber()).isEqualTo("123-45-67890");
        assertThat(user.getBusinessType()).isEqualTo("온라인 판매업");
        assertThat(user.getPhoneNumber()).isEqualTo("01012341234");
        assertThat(user.getMonthlyExpenseBudget()).isEqualTo(1_000_000);
        assertThat(user.getReceiptStartDate()).isEqualTo(LocalDate.of(2026, 9, 1));

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<Iterable> categoryCaptor = ArgumentCaptor.forClass(Iterable.class);
        then(categoryRepository).should().saveAll(categoryCaptor.capture());

        List<CategoryEntity> savedCategories = StreamSupport.stream(categoryCaptor.getValue().spliterator(), false).toList();

        List<String> expectedCategoryNames = Arrays.stream(DefaultCategory.values())
                .map(DefaultCategory::getCategoryName)
                .toList();

        assertThat(savedCategories).hasSize(DefaultCategory.values().length);

        assertThat(savedCategories)
                .extracting(CategoryEntity::getCategoryName)
                .containsExactlyInAnyOrderElementsOf(expectedCategoryNames);
    }

    @Test
    @DisplayName("signupToken이 없으면 인증 예외가 발생")
    public void completeSignupWithoutSignupToken() {
        // given
        SignupCompleteRequest request = mock(SignupCompleteRequest.class);

        // when & then
        assertThatThrownBy(() -> authService.completeSignup(null, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                                assertThat(((BusinessException) exception).getErrorCode())
                                .isEqualTo(AuthErrorCode.AUTHENTICATION_FAILED)
                );

        then(oAuthSignupSessionRepository).shouldHaveNoInteractions();
        then(userCompanyRepository).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("signupToken에 해당하는 가입 세션이 없으면 인증 예외가 발생")
    public void completesSignupWithoutSignupSessions() {
        // given
        String signupToken = "signup-token";
        SignupCompleteRequest request = mock(SignupCompleteRequest.class);

        given(oAuthSignupSessionRepository.find(signupToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.completeSignup(signupToken, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(((BusinessException) exception).getErrorCode())
                                .isEqualTo(AuthErrorCode.AUTHENTICATION_FAILED)
                );

        then(oAuthSignupSessionRepository).should().find(signupToken);

        then(userCompanyRepository).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 가입이 왼료된 사용자가 추가 회원가입을 요청하면 중복 가입 예외가 발생")
    public void completeSignupAlreadyActiveUser() {
        // given
        String signupToken = "signup-token";

        OAuthSignupSession session = new OAuthSignupSession("sns-id", OAuthProviderType.KAKAO.name());

        UserCompanyEntity user = UserCompanyEntity.createOAuthUser("sns-id", OAuthProviderType.KAKAO);

        // SIGNUP_REQUIRED -> ACTIVE
        user.completeSignup();

        SignupCompleteRequest request = mock(SignupCompleteRequest.class);

        given(oAuthSignupSessionRepository.find(signupToken)).willReturn(Optional.of(session));
        given(userCompanyRepository.findForSignupByOauthProviderAndSnsId(OAuthProviderType.KAKAO, "sns-id"))
                .willReturn(Optional.of(user));


        // when & then
        assertThatThrownBy(() -> authService.completeSignup(signupToken, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(((BusinessException) exception).getErrorCode())
                                .isEqualTo(AuthErrorCode.ALREADY_SIGNUP_COMPLETED)
                );

        then(categoryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("추가 회원가입 처리 성공 후 가입 세션을 삭제하고 Refresh Token을 발급")
    public void issueRefreshTokenAfterSignup() {
        // given
        String signupToken = "signup-token";
        Long userId = 1L;
        String refreshToken = "refresh-token";

        given(refreshTokenService.issue(userId)).willReturn(refreshToken);

        // when
        String result = authService.issueRefreshTokenAfterSignup(signupToken, userId);

        // then
        then(oAuthSignupSessionRepository).should().delete(signupToken);
        then(refreshTokenService).should().issue(userId);
        assertThat(result).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("유효한 Refresh Token이면 Access Token을 재발급하고 Refresh Token을 교체")
    public void reissueAccessToken() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String accessToken = "accessToken";

        Long userId = 1L;
        long expiration = 300L;
        Duration remainingTtl = Duration.ofHours(12);

        UserCompanyEntity user = mock(UserCompanyEntity.class);
        RotatedRefreshToken rotatedToken = mock(RotatedRefreshToken.class);

        when(refreshTokenService.validate(oldRefreshToken)).thenReturn(userId);
        when(userCompanyRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserStatus()).thenReturn(UserStatus.ACTIVE);
        when(jwtProvider.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtProvider.getAccessTokenExpirationSeconds()).thenReturn(expiration);
        when(refreshTokenService.rotate(oldRefreshToken, userId)).thenReturn(rotatedToken);
        when(rotatedToken.getRefreshToken()).thenReturn(newRefreshToken);
        when(rotatedToken.getRemainingTtl()).thenReturn(remainingTtl);

        // when
        AccessTokenReissueResult result = authService.reissue(oldRefreshToken);

        // then
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getExpiration()).isEqualTo(expiration);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(result.getRefreshTokenTtl()).isEqualTo(remainingTtl);
        then(refreshTokenService).should().validate(oldRefreshToken);
        then(refreshTokenService).should().rotate(oldRefreshToken, userId);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token이면 재발급 중단")
    public void reissueWithInvalidRefreshToken() {
        // given
        String refreshToken = "invalid-refresh-token";
        given(refreshTokenService.validate(refreshToken)).willThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                    assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN)
        );
        then(refreshTokenService).should().validate(refreshToken);
        then(userCompanyRepository).shouldHaveNoInteractions();
        then(jwtProvider).shouldHaveNoInteractions();
        then(refreshTokenService).should(never()).rotate(anyString(), anyLong());
    }

    @Test
    @DisplayName("Refresh Token의 사용자 ID가 DB에 존재하지 않으면 재발급 거부")
    public void reissueWithNonexistentUser() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        given(refreshTokenService.validate(refreshToken)).willReturn(userId);
        given(userCompanyRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN)
        );

        then(refreshTokenService).should().validate(refreshToken);
        then(userCompanyRepository).should().findById(userId);
        then(jwtProvider).shouldHaveNoInteractions();
        then(refreshTokenService).should(never()).rotate(anyString(), anyLong());
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 사용자는 Access Token 재발급 거부")
    public void reissueWithInactiveUser() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        // 신규 OAuth 사용자는 SIGNUP_REQUIRED 상태
        UserCompanyEntity user = UserCompanyEntity.createOAuthUser("sns-id", OAuthProviderType.KAKAO);

        given(refreshTokenService.validate(refreshToken)).willReturn(userId);
        given(userCompanyRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception ->
                        assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN)
        );

        then(refreshTokenService).should().validate(refreshToken);
        then(userCompanyRepository).should().findById(userId);
        then(jwtProvider).shouldHaveNoInteractions();
        then(refreshTokenService).should(never()).rotate(anyString(), anyLong());
    }

    @Test
    @DisplayName("Refresh Token 교체에 실패하면 Access Token 재발급 거부")
    public void reissueWhenRotationFails() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        UserCompanyEntity user = UserCompanyEntity.createOAuthUser("sns-id", OAuthProviderType.KAKAO);
        user.completeSignup();

        given(refreshTokenService.validate(refreshToken)).willReturn(userId);
        given(userCompanyRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtProvider.createAccessToken(userId)).willReturn("new-access-token");

        // 다른 요청이 먼저 Refresh Token을 교체한 상황
        given(refreshTokenService.rotate(refreshToken, userId)).willThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(((BusinessException) exception).getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN)
        );

        then(refreshTokenService).should().validate(refreshToken);
        then(userCompanyRepository).should().findById(userId);
        then(jwtProvider).should().createAccessToken(userId);
        then(refreshTokenService).should().rotate(anyString(), anyLong());
    }
}