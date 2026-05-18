package com.um.core.api.controller.profile;

import com.um.core.api.dto.profile.ProfileResponse;
import com.um.core.api.dto.profile.UpdateProfileRequest;
import com.um.core.api.security.UserPrincipal;
import com.um.core.common.web.ApiResponse;
import com.um.core.profile.application.ProfileApplicationService;
import com.um.core.profile.application.command.UpdateProfileCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/um/profile")
@RequiredArgsConstructor
@Tag(name = "用户资料")
public class ProfileController {

    private final ProfileApplicationService profileApplicationService;

    @GetMapping("/me")
    @Operation(summary = "当前用户资料")
    public ApiResponse<ProfileResponse> me() {
        Long userId = UserPrincipal.currentUserId();
        return ApiResponse.ok(ProfileResponse.from(profileApplicationService.getProfile(userId)));
    }

    @PutMapping("/me")
    @Operation(summary = "更新资料")
    public ApiResponse<ProfileResponse> update(@Valid @RequestBody UpdateProfileRequest req) {
        Long userId = UserPrincipal.currentUserId();
        var cmd = new UpdateProfileCommand(userId, req.nickname(), req.avatarUrl(),
                req.gender(), req.birthday(), req.extPatch());
        return ApiResponse.ok(ProfileResponse.from(profileApplicationService.updateProfile(cmd)));
    }
}
