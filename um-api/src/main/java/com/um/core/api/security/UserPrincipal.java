package com.um.core.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record UserPrincipal(Long userId, String deviceId) {

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p.userId();
        }
        return null;
    }

    public static String currentDeviceId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p.deviceId();
        }
        return "default";
    }
}
