package com.um.core.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPhoneRequest(
        @NotBlank @Pattern(regexp = "^1\\d{10}$") String phone,
        @NotBlank @Size(min = 4, max = 8) String smsCode,
        @NotBlank @Size(min = 8, max = 64) String password
) {
}
