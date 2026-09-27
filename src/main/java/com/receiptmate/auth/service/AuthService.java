package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.SignupCompleteRequest;
import com.receiptmate.auth.dto.SignupRequest;
import com.receiptmate.user.entity.OAuthProvider;
import com.receiptmate.user.entity.UserCompany;
import com.receiptmate.user.entity.UserStatus;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.auth.security.JwtTokenProvider;
import com.receiptmate.auth.security.RefreshTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCompanyRepository userCompanyRepository;
    private final SignupTokenService signupTokenService;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(
            OAuthProvider oauthProvider,
            String snsId,
            SignupRequest request
    ) {
        UserCompany user = new UserCompany(
                snsId,
                oauthProvider,
                request.companyName(),
                request.businessNumber(),
                request.businessType(),
                request.phoneNumber(),
                request.monthlyExpenseBudget(),
                request.receiptStartDate(),
                UserStatus.ACTIVE
        );

        UserCompany savedUser = userCompanyRepository.save(user);

        return savedUser.getUserId();
    }

    @Transactional
    public String signupComplete(
            String signupToken,
            SignupCompleteRequest request
    ) {
        String tokenValue =
                signupTokenService.get(signupToken);

        if (tokenValue == null) {
            throw new IllegalArgumentException("유효하지 않은 signupToken입니다.");
        }

        String[] tokenParts =
                tokenValue.split(":");

        OAuthProvider oauthProvider =
                OAuthProvider.valueOf(tokenParts[0]);

        String snsId =
                tokenParts[1];

        UserCompany user =
                userCompanyRepository
                        .findByOauthProviderAndSnsId(
                                oauthProvider,
                                snsId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "회원을 찾을 수 없습니다."
                                )
                        );

        user.completeSignup(
            request.companyName(),
            request.businessNumber(),
            request.businessType(),
            request.phoneNumber(),
            request.monthlyExpenseBudget(),
            request.receiptStartDate()
        );

        String refreshToken =
                refreshTokenProvider.generateToken();

        refreshTokenService.save(
                refreshToken,
                oauthProvider,
                snsId
        );

        signupTokenService.delete(signupToken);
        
        return refreshToken;
    }

        @Transactional(readOnly = true)
        public String reissueAccessToken(String refreshToken) {

        String tokenValue = refreshTokenService.get(refreshToken);

        if (tokenValue == null) {
                throw new IllegalArgumentException(
                        "유효하지 않은 Refresh Token입니다."
                );
        }

        String[] tokenParts = tokenValue.split(":");

        OAuthProvider oauthProvider =
                OAuthProvider.valueOf(tokenParts[0]);

        String snsId = tokenParts[1];

        UserCompany user =
                userCompanyRepository
                        .findByOauthProviderAndSnsId(
                                oauthProvider,
                                snsId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "회원을 찾을 수 없습니다."
                                )
                        );

        return jwtTokenProvider.generateAccessToken(
                user.getUserId()
        );
        }
}