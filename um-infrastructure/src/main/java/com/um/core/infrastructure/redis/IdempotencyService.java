package com.um.core.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final String PREFIX = "um:idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryAcquire(String key) {
        Boolean ok = redis.opsForValue().setIfAbsent(PREFIX + key, "1", TTL);
        return Boolean.TRUE.equals(ok);
    }
}
