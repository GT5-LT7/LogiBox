package com.spatra.gt5lt7.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // JWT 생성 - username 제거, managementId 추가
    public String createToken(UUID userId, String role, UUID managementId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("managementId", managementId != null ? managementId.toString() : null)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // JWT 파싱
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // userId 추출
    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    // role 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // managementId 추출 추가
    public UUID getManagementId(String token) {
        String managementId = getClaims(token).get("managementId", String.class);
        return managementId != null ? UUID.fromString(managementId) : null;
    }
}