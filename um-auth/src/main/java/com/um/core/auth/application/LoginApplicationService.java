package com.um.core.auth.application;

import com.um.core.auth.application.command.LoginCommand;
import com.um.core.auth.application.dto.AuthTokenResult;
import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.exception.BusinessException;
import com.um.core.domain.enums.UserStatusEnum;
import com.um.core.infrastructure.config.UmAuthProperties;
import com.um.core.infrastructure.persistence.po.SysUserPO;
import com.um.core.infrastructure.persistence.repository.UserRepository;
import com.um.core.infrastructure.redis.LoginLockService;
import com.um.core.infrastructure.redis.TokenStoreService;
import com.um.core.infrastructure.security.JwtTokenService;
import com.um.core.infrastructure.util.JsonExtHelper;
import com.um.core.vip.application.VipStatusResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoginApplicationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginLockService loginLockService;
    private final JwtTokenService jwtTokenService;
    private final TokenStoreService tokenStoreService;
    private final UmAuthProperties authProperties;
    private final VipStatusResolver vipStatusResolver;

    @Transactional(rollbackFor = Exception.class)
    public AuthTokenResult login(LoginCommand cmd) {
        String principal = cmd.principal();
        if (loginLockService.isLocked(principal)) {
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_LOCKED, "账号已锁定，请稍后再试");
        }
        SysUserPO user = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByPhone(principal))
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "用户名或密码错误"));

        if (user.getStatus() == UserStatusEnum.DEACTIVATED.getCode()) {
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_DEACTIVATED, "账号已注销");
        }
        if (user.getStatus() == UserStatusEnum.DISABLED.getCode()) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "账号已禁用");
        }
        if (!passwordEncoder.matches(cmd.password(), user.getPasswordHash())) {
            loginLockService.recordFailure(principal);
            log.warn("Login failed for principal={}", principal);
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "用户名或密码错误");
        }
        checkGeoRisk(user, cmd.clientIp());
        loginLockService.clearFailures(principal);
        user.setExtData(JsonExtHelper.merge(user.getExtData(), "lastLoginIp", cmd.clientIp()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        vipStatusResolver.resolve(user.getId());
        return issueTokens(user.getId(), cmd.deviceId(), cmd.clientIp());
    }

    public AuthTokenResult issueTokens(Long userId, String deviceId, String clientIp) {
        String device = deviceId != null ? deviceId : "default";
        JwtTokenService.TokenPair access = jwtTokenService.createAccessToken(userId, device);
        String refresh = tokenStoreService.issueRefreshToken(userId, device);
        log.info("Tokens issued userId={} deviceId={} ip={}", userId, device, clientIp);
        return AuthTokenResult.of(userId, access.accessToken(), refresh, access.expiresInSeconds());
    }

    @Transactional(readOnly = true)
    public AuthTokenResult refresh(Long userId, String deviceId, String refreshToken) {
        String device = deviceId != null ? deviceId : "default";
        if (!tokenStoreService.validateRefreshToken(userId, device, refreshToken)) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "Refresh Token 无效");
        }
        tokenStoreService.revokeRefresh(userId, device);
        return issueTokens(userId, device, null);
    }

    private void checkGeoRisk(SysUserPO user, String clientIp) {
        if (!authProperties.geoLoginAlertEnabled() || clientIp == null) {
            return;
        }
        String lastIp = JsonExtHelper.getString(user.getExtData(), "lastLoginIp");
        if (lastIp != null && !lastIp.equals(clientIp)) {
            log.warn("Geo login alert userId={} lastIp={} currentIp={}", user.getId(), lastIp, clientIp);
            throw new BusinessException(ErrorCodes.AUTH_GEO_RISK, "检测到异地登录，已拦截");
        }
    }
}
