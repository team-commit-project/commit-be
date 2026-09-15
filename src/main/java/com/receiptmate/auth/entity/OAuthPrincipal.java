package com.receiptmate.auth.entity;

import java.util.Map;

public interface OAuthPrincipal {

    Long getUserId();

    String getName();

    boolean isExisted();

    Map<String, Object> getAttributes();
}
