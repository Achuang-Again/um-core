package com.um.core.api.dto.profile;

import java.time.LocalDate;
import java.util.Map;

public record UpdateProfileRequest(
        String nickname,
        String avatarUrl,
        Integer gender,
        LocalDate birthday,
        Map<String, Object> extPatch
) {
}
