package com.um.core.api.dto.profile;

import com.um.core.infrastructure.persistence.po.SysUserProfilePO;

import java.time.LocalDate;

public record ProfileResponse(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer gender,
        LocalDate birthday,
        String extData
) {
    public static ProfileResponse from(SysUserProfilePO po) {
        return new ProfileResponse(po.getUserId(), po.getNickname(), po.getAvatarUrl(),
                po.getGender(), po.getBirthday(), po.getExtData());
    }
}
