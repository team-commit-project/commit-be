package com.receiptmate.auth.dto;

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
    private Map<String, Object> attributes;
    private boolean existed;
}
