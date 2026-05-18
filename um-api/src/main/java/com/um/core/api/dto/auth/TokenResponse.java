package com.um.core.api.dto.auth;

import com.um.core.auth.application.dto.AuthTokenResult;

public record TokenResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    public static TokenResponse from(AuthTokenResult r) {
        return new TokenResponse(r.userId(), r.accessToken(), r.refreshToken(), r.expiresIn(), r.tokenType());
    }
}
