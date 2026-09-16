package com.receiptmate.user.repository;

import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.type.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompanyEntity, Long> {

    Optional<UserCompanyEntity> findByOauthProviderAndSnsId(OAuthProvider oauthProvider, String snsId);
}
