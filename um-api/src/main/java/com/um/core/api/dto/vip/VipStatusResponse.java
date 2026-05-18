package com.um.core.api.dto.vip;

import com.um.core.domain.model.vip.VipStatus;

import java.time.LocalDateTime;

public record VipStatusResponse(
        boolean active,
        Integer levelId,
        String levelCode,
        String levelName,
        Integer weight,
        LocalDateTime expireTime
) {
    public static VipStatusResponse from(VipStatus s) {
        return new VipStatusResponse(s.active(), s.levelId(), s.levelCode(), s.levelName(),
                s.weight(), s.expireTime());
    }
}
