package com.receiptmate.auth.entity;

import com.receiptmate.user.entity.UserCompanyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loginHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserCompanyEntity userEntity;

    private String ipAddress;

    private String loginRegion;

    private LocalDateTime loginAt;

    public LoginHistoryEntity(UserCompanyEntity userEntity, String ipAddress, String loginRegion, LocalDateTime loginAt) {
        this.userEntity = userEntity;
        this.ipAddress = ipAddress;
        this.loginRegion = loginRegion;
        this.loginAt = loginAt;
    }
}
