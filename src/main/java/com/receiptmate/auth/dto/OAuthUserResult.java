package com.receiptmate.auth.dto;

import com.receiptmate.user.type.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class OAuthUserResult {

    private Long userId;
    private String snsId;
    private UserStatus userStatus;
    private Map<String, Object> attributes;
}
