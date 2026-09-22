package com.receiptmate.auth.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RefreshTokenProvider {

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}