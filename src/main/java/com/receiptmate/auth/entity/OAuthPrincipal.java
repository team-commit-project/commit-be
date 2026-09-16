package com.receiptmate.auth.entity;

import com.receiptmate.user.type.UserStatus;

import java.util.Map;

public interface OAuthPrincipal {

    Long getUserId();

    String getSnsId();

    UserStatus getUserStatus();

    Map<String, Object> getAttributes();
}
