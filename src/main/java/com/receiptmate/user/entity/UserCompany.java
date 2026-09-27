package com.receiptmate.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_company")
@Getter
@NoArgsConstructor
public class UserCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "sns_id", nullable = false, length = 100)
    private String snsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private OAuthProvider oauthProvider;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "business_number", length = 12)
    private String businessNumber;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(name = "monthly_expense_budget")
    private Integer monthlyExpenseBudget;

    @Column(name = "receipt_start_date")
    private LocalDate receiptStartDate;

    @Column(name = "last_login_region", length = 100)
    private String lastLoginRegion;

    // enum으로 변경
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 50)
    private UserStatus userStatus;

    public UserCompany(
        String snsId,
        OAuthProvider oauthProvider,
        String companyName,
        String businessNumber,
        String businessType,
        String phoneNumber,
        Integer monthlyExpenseBudget,
        LocalDate receiptStartDate,
        UserStatus userStatus
    ) {
        this.snsId = snsId;
        this.oauthProvider = oauthProvider;
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.businessType = businessType;
        this.phoneNumber = phoneNumber;
        this.monthlyExpenseBudget = monthlyExpenseBudget;
        this.receiptStartDate = receiptStartDate;
        this.userStatus = userStatus;
    }

    public UserCompany(
        String snsId,
        OAuthProvider oauthProvider,
        UserStatus userStatus
    ) {
        this.snsId = snsId;
        this.oauthProvider = oauthProvider;
        this.userStatus = userStatus;
    }

    public boolean isAdditionalInfoCompleted() {
      return companyName != null
            && businessNumber != null
            && businessType != null
            && phoneNumber != null
            && monthlyExpenseBudget != null
            && receiptStartDate != null;
    }

    public void completeSignup(
        String companyName,
        String businessNumber,
        String businessType,
        String phoneNumber,
        Integer monthlyExpenseBudget,
        LocalDate receiptStartDate
    ) {
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.businessType = businessType;
        this.phoneNumber = phoneNumber;
        this.monthlyExpenseBudget = monthlyExpenseBudget;
        this.receiptStartDate = receiptStartDate;
        this.userStatus = UserStatus.ACTIVE;
    }
}
