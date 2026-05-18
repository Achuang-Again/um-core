package com.um.core.vip.domain;

import com.um.core.infrastructure.persistence.po.SysVipLevelPO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * VIP 升级：按剩余天数与月费比例折算新等级时长。
 */
public final class VipUpgradeCalculator {

    private VipUpgradeCalculator() {
    }

    public static LocalDateTime calculateNewExpire(
            LocalDateTime currentExpire,
            SysVipLevelPO oldLevel,
            SysVipLevelPO newLevel,
            int purchasedDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseExpire = currentExpire.isAfter(now) ? currentExpire : now;

        long remainingDays = ChronoUnit.DAYS.between(now, baseExpire);
        if (remainingDays < 0) {
            remainingDays = 0;
        }

        BigDecimal oldDaily = oldLevel.getMonthlyPrice()
                .divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP);
        BigDecimal newDaily = newLevel.getMonthlyPrice()
                .divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP);

        BigDecimal remainingValue = oldDaily.multiply(BigDecimal.valueOf(remainingDays));
        long convertedDays = 0;
        if (newDaily.compareTo(BigDecimal.ZERO) > 0) {
            convertedDays = remainingValue.divide(newDaily, 0, RoundingMode.DOWN).longValue();
        }
        return now.plusDays(convertedDays + purchasedDays);
    }
}
