package com.receiptmate.auth.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 생성
    public String create(String userId, String userStatus) {
        Date expiration =
                Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));

        Key key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .claim("userId", userId)
                .claim("userStatus", userStatus)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 검증 후 userId 반환
    public String validateAccessToken(String jwt) {

        Key key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}
