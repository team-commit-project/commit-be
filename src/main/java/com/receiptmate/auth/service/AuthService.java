package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthSignupSession;
import com.receiptmate.auth.dto.request.SignupCompleteRequest;
import com.receiptmate.auth.exception.AuthErrorCode;
import com.receiptmate.auth.repository.OAuthSignupSessionRepository;
import com.receiptmate.category.entity.CategoryEntity;
import com.receiptmate.category.repository.CategoryRepository;
import com.receiptmate.category.type.DefaultCategory;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.user.type.OAuthProviderType;
import com.receiptmate.user.type.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final OAuthSignupSessionRepository oAuthSignupSessionRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CategoryRepository categoryRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public Long completeSignup(String signupToken, SignupCompleteRequest request) {
        OAuthSignupSession session = validateSignupToken(signupToken);
        OAuthProviderType provider = resolveProvider(session.getJoinType());

        UserCompanyEntity user = userCompanyRepository
                .findForSignupByOauthProviderAndSnsId(provider, session.getSnsId())
                .orElseThrow(() -> {
                    log.warn("추가 회원가입 대상 사용자를 찾을 수 없습니다. provider={}", provider);
                    return new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
                });


        if (user.getUserStatus() == UserStatus.ACTIVE) {
            log.warn("이미 회원가입이 완료된 사용자가 추가 회원가입을 요청했습니다. snsId={}", session.getSnsId());
            throw new BusinessException(AuthErrorCode.ALREADY_SIGNUP_COMPLETED);
        }

        user.updateSignupInfo(
                request.getCompanyName(),
                request.getBusinessNumber(),
                request.getBusinessType(),
                request.getPhoneNumber(),
                request.getMonthlyExpenseBudget(),
                request.getReceiptStartDate()
        );

        activateDefaultCategories(user);
        user.completeSignup();

        return user.getUserId();
    }

    // completeSignup의 트랜잭션 커밋이 성공한 뒤 외부 호출자가 실행한다.
    @Transactional(propagation = Propagation.NEVER)
    public String issueRefreshTokenAfterSignup(String signupToken, Long userId) {
        oAuthSignupSessionRepository.delete(signupToken);

        String refreshToken = refreshTokenService.issue(userId);

        log.info("추가 회원가입 완료 후 로그인 인증 발급이 완료되었습니다. userId={}", userId);

        return refreshToken;
    }

    private OAuthSignupSession validateSignupToken(String signupToken) {
        if (!StringUtils.hasText(signupToken)) {
            log.warn("추가 회원가입 요청에 signupToken이 존재하지 않습니다.");
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        OAuthSignupSession session = oAuthSignupSessionRepository.find(signupToken)
                .orElseThrow(() -> {
                    log.warn("유효한 OAuth 회원가입 세션을 찾을 수 없습니다.");
                    return new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
                });

        if (!StringUtils.hasText(session.getSnsId()) || !StringUtils.hasText(session.getJoinType())) {
            log.warn("OAuth 회원가입 세션 데이터가 올바르지 않습니다.");
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        return session;
    }

    private OAuthProviderType resolveProvider(String joinType) {
        try {
            return OAuthProviderType.valueOf(joinType);
        } catch (IllegalArgumentException e) {
            log.warn("OAuth 회원가입 세션의 provider 값이 올바르지 않습니다. joinType={}", joinType);
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
        }
    }

    private void activateDefaultCategories(UserCompanyEntity user) {
        List<CategoryEntity> newCategoryEntities = Arrays.stream(DefaultCategory.values())
                .map(defaultCategory -> CategoryEntity.create(user, defaultCategory.getCategoryName()))
                .toList();


        categoryRepository.saveAll(newCategoryEntities);
    }
}
