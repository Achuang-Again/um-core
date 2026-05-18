package com.um.core.infrastructure.security;

import com.um.core.infrastructure.config.UmAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Access Token 签发与解析。
 */
@Service
public class JwtTokenService {

    private final SecretKey key;
    private final UmAuthProperties properties;

    public JwtTokenService(UmAuthProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair createAccessToken(Long userId, String deviceId) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plus(properties.accessTokenTtl());
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .issuer("um-core")
                .claim("device_id", deviceId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new TokenPair(token, jti, properties.accessTokenTtl().getSeconds());
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenPair(String accessToken, String jti, long expiresInSeconds) {
    }
}
