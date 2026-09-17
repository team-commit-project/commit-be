package com.receiptmate.user.repository;

import com.receiptmate.user.entity.OAuthProvider;
import com.receiptmate.user.entity.UserCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {

    Optional<UserCompany> findByOauthProviderAndSnsId(
            OAuthProvider oauthProvider,
            String snsId
    );
}
