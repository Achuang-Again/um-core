package com.um.core.auth.application;

import com.um.core.auth.application.command.RegisterByPhoneCommand;
import com.um.core.auth.application.command.RegisterByUsernameCommand;
import com.um.core.auth.application.dto.AuthTokenResult;
import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.exception.BusinessException;
import com.um.core.domain.enums.RegisterTypeEnum;
import com.um.core.domain.enums.UserStatusEnum;
import com.um.core.infrastructure.persistence.po.SysUserPO;
import com.um.core.infrastructure.persistence.repository.UserRepository;
import com.um.core.infrastructure.redis.SmsCodeService;
import com.um.core.infrastructure.util.JsonExtHelper;
import com.um.core.profile.application.ProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 多方式注册：用户名、手机号（邮箱/OAuth 可扩展）。
 */
@Service
@RequiredArgsConstructor
public class RegisterApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RegisterApplicationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileApplicationService profileApplicationService;
    private final LoginApplicationService loginApplicationService;
    private final SmsCodeService smsCodeService;

    @Transactional(rollbackFor = Exception.class)
    public AuthTokenResult registerByUsername(RegisterByUsernameCommand cmd) {
        validatePassword(cmd.password());
        if (userRepository.existsUsername(cmd.username())) {
            throw new BusinessException(ErrorCodes.AUTH_USERNAME_EXISTS, "用户名已存在");
        }
        SysUserPO user = buildUser(cmd.username(), cmd.password(), null, null, cmd.clientIp(), RegisterTypeEnum.USERNAME);
        userRepository.save(user);
        profileApplicationService.initProfile(user.getId(), null);
        log.info("User registered by username userId={}", user.getId());
        return loginApplicationService.issueTokens(user.getId(), "default", cmd.clientIp());
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthTokenResult registerByPhone(RegisterByPhoneCommand cmd) {
        if (!smsCodeService.verify(cmd.phone(), cmd.smsCode())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_SMS_CODE, "验证码错误或已过期");
        }
        validatePassword(cmd.password());
        if (userRepository.existsPhone(cmd.phone())) {
            throw new BusinessException(ErrorCodes.AUTH_PHONE_EXISTS, "手机号已注册");
        }
        String username = "p_" + cmd.phone();
        SysUserPO user = buildUser(username, cmd.password(), cmd.phone(), null, cmd.clientIp(), RegisterTypeEnum.PHONE);
        userRepository.save(user);
        profileApplicationService.initProfile(user.getId(), null);
        return loginApplicationService.issueTokens(user.getId(), "default", cmd.clientIp());
    }

    private SysUserPO buildUser(String username, String rawPassword, String phone, String email,
                                String clientIp, RegisterTypeEnum type) {
        SysUserPO user = new SysUserPO();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(UserStatusEnum.NORMAL.getCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setCreateBy("register");
        user.setUpdateBy("register");
        user.setExtData(JsonExtHelper.merge(null, "registerType", type.name()));
        user.setExtData(JsonExtHelper.merge(user.getExtData(), "registerIp", clientIp));
        return user;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException(ErrorCodes.PARAM_INVALID, "密码长度至少8位");
        }
    }
}
