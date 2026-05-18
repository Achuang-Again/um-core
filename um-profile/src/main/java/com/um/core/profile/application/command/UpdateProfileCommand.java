package com.um.core.profile.application.command;

import java.time.LocalDate;
import java.util.Map;

public record UpdateProfileCommand(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer gender,
        LocalDate birthday,
        Map<String, Object> extPatch
) {
}
