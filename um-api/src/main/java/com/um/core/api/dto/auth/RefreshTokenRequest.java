package com.um.core.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull Long userId,
        @NotBlank String refreshToken,
        String deviceId
) {
}
