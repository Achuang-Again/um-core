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
import com.um.core.vip.application.VipStatusResolver;
import com.um.core.domain.model.vip.VipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginLockService loginLockService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private TokenStoreService tokenStoreService;
    @Mock
    private UmAuthProperties authProperties;
    @Mock
    private VipStatusResolver vipStatusResolver;

    @InjectMocks
    private LoginApplicationService loginApplicationService;

    @Test
    @DisplayName("场景：用户名密码正确，返回 Token")
    void login_shouldReturnToken_whenCredentialsValid() {
        SysUserPO user = activeUser();
        when(loginLockService.isLocked("alice")).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass12345", user.getPasswordHash())).thenReturn(true);
        when(jwtTokenService.createAccessToken(1L, "d1"))
                .thenReturn(new JwtTokenService.TokenPair("access", "jti", 7200));
        when(tokenStoreService.issueRefreshToken(1L, "d1")).thenReturn("refresh");
        when(vipStatusResolver.resolve(1L)).thenReturn(VipStatus.none());

        AuthTokenResult result = loginApplicationService.login(
                new LoginCommand("alice", "pass12345", "d1", "127.0.0.1"));

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        verify(loginLockService).clearFailures("alice");
    }

    @Test
    @DisplayName("场景：密码错误，抛出认证失败并记录失败次数")
    void login_shouldThrow_whenPasswordWrong() {
        SysUserPO user = activeUser();
        when(loginLockService.isLocked("alice")).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> loginApplicationService.login(
                new LoginCommand("alice", "wrong", "d1", "127.0.0.1")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCodes.AUTH_INVALID_CREDENTIALS);

        verify(loginLockService).recordFailure("alice");
    }

    @Test
    @DisplayName("场景：账号已锁定，拒绝登录")
    void login_shouldThrow_whenAccountLocked() {
        when(loginLockService.isLocked("alice")).thenReturn(true);

        assertThatThrownBy(() -> loginApplicationService.login(
                new LoginCommand("alice", "pass12345", "d1", "127.0.0.1")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCodes.AUTH_ACCOUNT_LOCKED);
    }

    private static SysUserPO activeUser() {
        SysUserPO user = new SysUserPO();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hash");
        user.setStatus(UserStatusEnum.NORMAL.getCode());
        return user;
    }
}
