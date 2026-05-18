package com.um.core.vip.domain;

import com.um.core.infrastructure.persistence.po.SysVipLevelPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VIP 升级折算算法测试。
 */
class VipUpgradeCalculatorTest {

    @Test
    @DisplayName("场景：剩余30天青铜升黄金，应折算并加上购买天数")
    void shouldExtendExpire_whenUpgradeFromBronzeToGold() {
        SysVipLevelPO bronze = level("BRONZE", "19.90");
        SysVipLevelPO gold = level("GOLD", "79.90");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpire = now.plusDays(30);

        LocalDateTime result = VipUpgradeCalculator.calculateNewExpire(currentExpire, bronze, gold, 30);

        assertThat(result).isAfter(now.plusDays(30));
    }

    @Test
    @DisplayName("场景：已过期用户升级，从当前时间起算购买天数")
    void shouldStartFromNow_whenAlreadyExpired() {
        SysVipLevelPO bronze = level("BRONZE", "19.90");
        SysVipLevelPO gold = level("GOLD", "79.90");
        LocalDateTime pastExpire = LocalDateTime.now().minusDays(1);

        LocalDateTime result = VipUpgradeCalculator.calculateNewExpire(pastExpire, bronze, gold, 7);

        assertThat(result).isAfter(LocalDateTime.now().plusDays(6));
    }

    private static SysVipLevelPO level(String code, String price) {
        SysVipLevelPO po = new SysVipLevelPO();
        po.setLevelCode(code);
        po.setMonthlyPrice(new BigDecimal(price));
        return po;
    }
}
