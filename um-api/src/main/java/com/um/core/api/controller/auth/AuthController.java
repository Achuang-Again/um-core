package com.um.core.api.controller.auth;

import com.um.core.api.dto.auth.*;
import com.um.core.api.security.UserPrincipal;
import com.um.core.auth.application.*;
import com.um.core.auth.application.command.LoginCommand;
import com.um.core.auth.application.command.RegisterByPhoneCommand;
import com.um.core.auth.application.command.RegisterByUsernameCommand;
import com.um.core.auth.application.dto.AuthTokenResult;
import com.um.core.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/um/auth")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {

    private final RegisterApplicationService registerApplicationService;
    private final LoginApplicationService loginApplicationService;
    private final LogoutApplicationService logoutApplicationService;
    private final DeactivateApplicationService deactivateApplicationService;
    private final SmsCodeApplicationService smsCodeApplicationService;

    @PostMapping("/register/username")
    @Operation(summary = "用户名密码注册")
    public ApiResponse<TokenResponse> registerUsername(@Valid @RequestBody RegisterUsernameRequest req,
                                                       HttpServletRequest http) {
        AuthTokenResult result = registerApplicationService.registerByUsername(
                new RegisterByUsernameCommand(req.username(), req.password(), clientIp(http)));
        return ApiResponse.ok(TokenResponse.from(result));
    }

    @PostMapping("/register/phone")
    @Operation(summary = "手机号验证码注册")
    public ApiResponse<TokenResponse> registerPhone(@Valid @RequestBody RegisterPhoneRequest req,
                                                    HttpServletRequest http) {
        AuthTokenResult result = registerApplicationService.registerByPhone(
                new RegisterByPhoneCommand(req.phone(), req.smsCode(), req.password(), clientIp(http)));
        return ApiResponse.ok(TokenResponse.from(result));
    }

    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码")
    public ApiResponse<Void> sendSms(@Valid @RequestBody SendSmsRequest req) {
        smsCodeApplicationService.sendLoginCode(req.phone());
        return ApiResponse.ok(null);
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        AuthTokenResult result = loginApplicationService.login(
                new LoginCommand(req.principal(), req.password(), req.deviceId(), clientIp(http)));
        return ApiResponse.ok(TokenResponse.from(result));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        AuthTokenResult result = loginApplicationService.refresh(req.userId(), req.deviceId(), req.refreshToken());
        return ApiResponse.ok(TokenResponse.from(result));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = UserPrincipal.currentUserId();
        String access = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : null;
        logoutApplicationService.logout(userId, UserPrincipal.currentDeviceId(), access);
        return ApiResponse.ok(null);
    }

    @PostMapping("/kick-all")
    @Operation(summary = "踢掉所有端会话")
    public ApiResponse<Void> kickAll() {
        logoutApplicationService.kickAllDevices(UserPrincipal.currentUserId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/deactivate")
    @Operation(summary = "注销账号")
    public ApiResponse<Void> deactivate(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = UserPrincipal.currentUserId();
        String access = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : null;
        deactivateApplicationService.deactivate(userId, UserPrincipal.currentDeviceId(), access);
        return ApiResponse.ok(null);
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
