package com.um.core.auth.application;

import com.um.core.infrastructure.config.UmAuthProperties;
import com.um.core.infrastructure.redis.TokenStoreService;
import com.um.core.infrastructure.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutApplicationService {

    private final TokenStoreService tokenStoreService;
    private final JwtTokenService jwtTokenService;
    private final UmAuthProperties authProperties;

    public void logout(Long userId, String deviceId, String accessToken) {
        tokenStoreService.revokeRefresh(userId, deviceId != null ? deviceId : "default");
        if (accessToken != null) {
            try {
                Claims claims = jwtTokenService.parse(accessToken);
                Date exp = claims.getExpiration();
                Duration ttl = Duration.between(Instant.now(), exp.toInstant());
                if (!ttl.isNegative()) {
                    tokenStoreService.blacklistAccessToken(claims.getId(), ttl);
                }
            } catch (Exception ignored) {
                // 已过期 token 忽略
            }
        }
    }

    /** 踢掉该用户所有端会话（SSO 踢人） */
    public void kickAllDevices(Long userId) {
        tokenStoreService.revokeAllDevices(userId);
    }
}
