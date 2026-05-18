package com.um.core.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 短信验证码（开发环境可固定为配置值；生产应对接短信网关）。
 */
@Service
public class SmsCodeService {

    private static final String PREFIX = "um:sms:code:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public SmsCodeService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String generateAndStore(String phone) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        redis.opsForValue().set(PREFIX + phone, code, TTL);
        return code;
    }

    public boolean verify(String phone, String code) {
        String stored = redis.opsForValue().get(PREFIX + phone);
        if (stored == null || code == null) {
            return false;
        }
        boolean ok = stored.equals(code);
        if (ok) {
            redis.delete(PREFIX + phone);
        }
        return ok;
    }
}
