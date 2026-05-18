package com.um.core.vip.application.command;

import java.math.BigDecimal;

public record VipSubscribeCommand(
        Long userId,
        String levelCode,
        int durationDays,
        String orderNo,
        BigDecimal paidAmount,
        String idempotencyKey
) {
}
