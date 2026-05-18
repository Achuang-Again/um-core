package com.um.core.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUsernameRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 8, max = 64) String password
) {
}
