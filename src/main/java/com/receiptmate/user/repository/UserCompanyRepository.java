package com.receiptmate.user.repository;

import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.type.OAuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompanyEntity, Long> {

    Optional<UserCompanyEntity> findByOauthProviderAndSnsId(OAuthProviderType oauthProvider, String snsId);
}
