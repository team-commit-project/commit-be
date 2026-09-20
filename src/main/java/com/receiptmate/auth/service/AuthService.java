package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.SignupRequest;
import com.receiptmate.user.entity.OAuthProvider;
import com.receiptmate.user.entity.UserCompany;
import com.receiptmate.user.repository.UserCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCompanyRepository userCompanyRepository;

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
                "ACTIVE"
        );

        UserCompany savedUser = userCompanyRepository.save(user);

        return savedUser.getUserId();
    }
}