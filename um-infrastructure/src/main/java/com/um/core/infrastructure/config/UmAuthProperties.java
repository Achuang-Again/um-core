package com.um.core.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "um.auth")
public record UmAuthProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        int maxLoginAttempts,
        Duration lockDuration,
        boolean geoLoginAlertEnabled
) {
    public UmAuthProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofHours(2);
        }
        if (refreshTokenTtl == null) {
            refreshTokenTtl = Duration.ofDays(30);
        }
        if (lockDuration == null) {
            lockDuration = Duration.ofMinutes(30);
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            jwtSecret = "dev-only-change-in-production-32chars!!";
        }
    }
}
