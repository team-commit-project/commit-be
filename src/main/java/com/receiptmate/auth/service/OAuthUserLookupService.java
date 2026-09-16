package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import com.receiptmate.common.exception.BusinessException;
import com.receiptmate.common.exception.CommonErrorCode;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.user.type.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserLookupService {

    private final UserCompanyRepository userCompanyRepository;

    @Transactional
    public OAuthUserResult findUser(String registration, String snsId, Map<String, Object> providerAttributes) {

        OAuthProvider oAuthProvider = OAuthProvider.valueOf(registration);

        Optional<UserCompanyEntity> existingUser = userCompanyRepository.findByOauthProviderAndSnsId(oAuthProvider, snsId);

        UserCompanyEntity userCompany;

        // 기존 회원
        if (existingUser.isPresent()) {
            userCompany = existingUser.get();
        }
        // 최초 SNS 로그인 사용자
        else {
            userCompany = UserCompanyEntity.createOAuthUser(snsId, oAuthProvider);
            userCompany = userCompanyRepository.save(userCompany);
        }

        Long userId = userCompany.getUserId();
        if (userId == null) {
            log.error("OAuth 사용자 조회/저장 후 userId가 존재하지 않습니다. provide={}", oAuthProvider);
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> attributes = new HashMap<>(providerAttributes);
        attributes.put("snsId", snsId);
        attributes.put("joinType", registration);

        return new OAuthUserResult(userId, snsId, userCompany.getUserStatus(), attributes);
    }

}
