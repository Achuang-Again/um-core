package com.um.core.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendSmsRequest(
        @NotBlank @Pattern(regexp = "^1\\d{10}$") String phone
) {
}
