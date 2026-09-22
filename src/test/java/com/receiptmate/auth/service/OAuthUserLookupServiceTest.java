package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import com.receiptmate.user.entity.UserCompanyEntity;
import com.receiptmate.user.repository.UserCompanyRepository;
import com.receiptmate.user.type.OAuthProviderType;
import com.receiptmate.user.type.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OAuthUserLookupService.class)
class OAuthUserLookupServiceTest {

    @Autowired
    private OAuthUserLookupService oAuthUserLookupService;

    @Autowired
    private UserCompanyRepository userCompanyRepository;


    @Test
    @DisplayName("최초 OAuth 로그인 사용자는 SIGNUP_REQUIRED 상태로 저장된다.")
    public void createOAuthUser() {
        // given
        String registration = "KAKAO";
        String snsId = "12345";
        HashMap<String, Object> attributes = new HashMap<>();


        // when
        OAuthUserResult result = oAuthUserLookupService.findUser(registration, snsId, attributes);

        // then
        UserCompanyEntity savedUser = userCompanyRepository.findById(result.getUserId()).orElseThrow();;
        assertThat(savedUser.getUserStatus()).isEqualTo(UserStatus.SIGNUP_REQUIRED);
    }

    @Test
    @DisplayName("기존 OAuth 사용자가 다시 로그인하면 신규 사용자를 생성하지 않는다.")
    public void findExistingOAuthUser() {
        // given
        String registration = "KAKAO";
        String snsId = "12345";
        HashMap<String, Object> attributes = new HashMap<>();

        OAuthUserResult firstResult = oAuthUserLookupService.findUser(registration, snsId, attributes);

        // when
        OAuthUserResult secondResult = oAuthUserLookupService.findUser(registration, snsId, attributes);

        // then
        assertThat(secondResult.getUserId()).isEqualTo(firstResult.getUserId());
    }

    @Test
    @DisplayName("같은 SnsId라도 OAuth Provider가 다르면 서로 다른 사용자로 저장된다.")
    public void createDifferentUserByProvider() {
        // given
        String snsId = "same-sns-id";

        // when
        OAuthUserResult kakaoUser = oAuthUserLookupService.findUser("KAKAO", snsId, new HashMap<>());
        OAuthUserResult naverUser = oAuthUserLookupService.findUser("NAVER", snsId, new HashMap<>());

        // then
        assertThat(kakaoUser.getUserId()).isNotEqualTo(naverUser.getUserId());
    }

}