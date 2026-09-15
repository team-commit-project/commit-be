package com.receiptmate.auth.service;

import com.receiptmate.auth.dto.OAuthUserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthUserLookupService {

    public OAuthUserResult findUser(String registration, String snsId, Map<String, Object> providerAttributes) {
        // todo: 회원 조회

        Map<String, Object> attributes = new HashMap<>(providerAttributes);
        attributes.put("snsId", snsId);
        attributes.put("joinType", registration);

        // todo: 기존 회원

        // 신규 회원
        return new OAuthUserResult(null, snsId, attributes, false);
    }

}
