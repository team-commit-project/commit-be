package com.receiptmate.user.entity;


import com.receiptmate.user.type.OAuthProvider;
import com.receiptmate.user.type.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String snsId;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    private String companyName;

    private String businessNumber;

    private String businessType;

    private String phoneNumber;

    private Integer monthlyExpenseBudget;

    private LocalDate receiptStartDate;

    private String lastLoginRegion;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private UserCompanyEntity(String snsId, OAuthProvider oauthProvider, UserStatus userStatus) {
        this.snsId = snsId;
        this.oauthProvider = oauthProvider;
        this.userStatus = userStatus;
    }

    public static UserCompanyEntity createOAuthUser(String snsId, OAuthProvider oauthProvider) {
        return new UserCompanyEntity(snsId, oauthProvider, UserStatus.SIGNUP_REQUIRED);
    }
}
