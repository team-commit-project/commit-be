package com.receiptmate.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum OAuthProviderType {
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    private final String registrationId;

    public static Optional<OAuthProviderType> fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(provider -> provider.registrationId.equals(registrationId))
                .findFirst();
    }
}
