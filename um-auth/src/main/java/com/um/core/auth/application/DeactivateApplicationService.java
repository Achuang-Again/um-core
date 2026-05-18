package com.um.core.auth.application;

import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.exception.BusinessException;
import com.um.core.domain.enums.UserStatusEnum;
import com.um.core.infrastructure.persistence.po.SysUserPO;
import com.um.core.infrastructure.persistence.repository.UserRepository;
import com.um.core.infrastructure.redis.TokenStoreService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 账号注销：软业务状态 + 敏感字段匿名化，保留主键供宿主关联历史数据。
 */
@Service
@RequiredArgsConstructor
public class DeactivateApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DeactivateApplicationService.class);
    private static final String ANON_SALT = "um-core-anon";

    private final UserRepository userRepository;
    private final TokenStoreService tokenStoreService;
    private final LogoutApplicationService logoutApplicationService;

    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long userId, String deviceId, String accessToken) {
        SysUserPO user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "用户不存在"));
        if (user.getStatus() == UserStatusEnum.DEACTIVATED.getCode()) {
            return;
        }
        String phoneHash = user.getPhone() != null ? hash(user.getPhone()) : null;
        String emailHash = user.getEmail() != null ? hash(user.getEmail()) : null;

        user.setUsername("deleted_" + user.getId());
        user.setPhone(phoneHash != null ? "h_" + phoneHash.substring(0, Math.min(18, phoneHash.length())) : null);
        user.setEmail(emailHash != null ? "h_" + emailHash.substring(0, Math.min(18, emailHash.length())) + "@anon.local" : null);
        user.setPasswordHash("");
        user.setStatus(UserStatusEnum.DEACTIVATED.getCode());
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("deactivate");
        userRepository.save(user);

        logoutApplicationService.logout(userId, deviceId, accessToken);
        tokenStoreService.revokeAllDevices(userId);
        log.info("User deactivated userId={}", userId);
    }

    private static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((ANON_SALT + value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
