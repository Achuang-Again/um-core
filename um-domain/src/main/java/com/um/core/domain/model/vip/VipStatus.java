package com.um.core.domain.model.vip;

import java.time.LocalDateTime;

/**
 * VIP 查询结果（含惰性过期后的展示态）。
 */
public record VipStatus(
        boolean active,
        Integer levelId,
        String levelCode,
        String levelName,
        Integer weight,
        LocalDateTime expireTime
) {
    public static VipStatus none() {
        return new VipStatus(false, null, null, null, null, null);
    }
}
