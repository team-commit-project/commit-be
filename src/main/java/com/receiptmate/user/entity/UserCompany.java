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
    @Column(name = "user_status", nullable = false, length = 50)
    private String userStatus; 
}
