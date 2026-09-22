package com.receiptmate.user.repository;

import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.type.OAuthProviderType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompanyEntity, Long> {

    Optional<UserCompanyEntity> findByOauthProviderAndSnsId(OAuthProviderType oauthProvider, String snsId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserCompanyEntity> findForSignupByOauthProviderAndSnsId(OAuthProviderType oauthProvider, String snsId);
}
