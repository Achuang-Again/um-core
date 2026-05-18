package com.um.core.infrastructure.redis;

import com.um.core.infrastructure.config.UmAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginLockService {

    private static final String FAIL_PREFIX = "um:login:fail:";

    private final StringRedisTemplate redis;
    private final UmAuthProperties properties;

    public LoginLockService(StringRedisTemplate redis, UmAuthProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public boolean isLocked(String username) {
        String v = redis.opsForValue().get(FAIL_PREFIX + username);
        if (v == null) {
            return false;
        }
        return Integer.parseInt(v) >= properties.maxLoginAttempts();
    }

    public void recordFailure(String username) {
        String key = FAIL_PREFIX + username;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, properties.lockDuration());
        }
    }

    public void clearFailures(String username) {
        redis.delete(FAIL_PREFIX + username);
    }
}
