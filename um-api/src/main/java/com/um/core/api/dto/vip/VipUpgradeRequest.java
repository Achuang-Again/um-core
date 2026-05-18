package com.um.core.api.dto.vip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VipUpgradeRequest(
        @NotBlank String newLevelCode,
        @Positive int purchasedDays,
        @NotBlank String orderNo,
        @NotNull BigDecimal paidAmount,
        String idempotencyKey
) {
}
