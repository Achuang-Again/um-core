package com.um.core.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String principal,
        @NotBlank String password,
        String deviceId
) {
}
