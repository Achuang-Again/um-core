package com.um.core.auth.application.dto;

public record AuthTokenResult(
        Long userId,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    public static AuthTokenResult of(Long userId, String access, String refresh, long expiresIn) {
        return new AuthTokenResult(userId, access, refresh, expiresIn, "Bearer");
    }
}
