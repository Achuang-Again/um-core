package com.um.core.infrastructure.redis;

import com.um.core.infrastructure.config.UmAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Refresh Token 与 Access 黑名单（踢人、登出）。
 */
@Service
public class TokenStoreService {

    private static final String REFRESH_PREFIX = "um:refresh:";
    private static final String BLACKLIST_PREFIX = "um:token:blacklist:";

    private final StringRedisTemplate redis;
    private final UmAuthProperties properties;

    public TokenStoreService(StringRedisTemplate redis, UmAuthProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public String issueRefreshToken(Long userId, String deviceId) {
        String token = UUID.randomUUID().toString();
        String key = refreshKey(userId, deviceId);
        redis.opsForValue().set(key, token, properties.refreshTokenTtl());
        return token;
    }

    public boolean validateRefreshToken(Long userId, String deviceId, String refreshToken) {
        String stored = redis.opsForValue().get(refreshKey(userId, deviceId));
        return refreshToken != null && refreshToken.equals(stored);
    }

    public void revokeRefresh(Long userId, String deviceId) {
        redis.delete(refreshKey(userId, deviceId));
    }

    public void revokeAllDevices(Long userId) {
        Set<String> keys = redis.keys(REFRESH_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    public void blacklistAccessToken(String jti, Duration ttl) {
        if (jti != null) {
            redis.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttl);
        }
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
    }

    private static String refreshKey(Long userId, String deviceId) {
        return REFRESH_PREFIX + userId + ":" + (deviceId == null ? "default" : deviceId);
    }
}
