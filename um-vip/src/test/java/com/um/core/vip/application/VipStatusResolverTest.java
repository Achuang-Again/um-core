package com.um.core.vip.application;

import com.um.core.domain.enums.VipUserStatusEnum;
import com.um.core.domain.model.vip.VipStatus;
import com.um.core.infrastructure.persistence.mapper.SysUserVipMapper;
import com.um.core.infrastructure.persistence.mapper.SysVipLevelMapper;
import com.um.core.infrastructure.persistence.po.SysUserVipPO;
import com.um.core.infrastructure.persistence.po.SysVipLevelPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VipStatusResolverTest {

    @Mock
    private SysUserVipMapper userVipMapper;
    @Mock
    private SysVipLevelMapper vipLevelMapper;
    @Mock
    private VipExpireService vipExpireService;

    @InjectMocks
    private VipStatusResolver resolver;

    @Test
    @DisplayName("场景：无 VIP 记录，返回非会员")
    void resolve_shouldReturnNone_whenNoRecord() {
        when(userVipMapper.selectById(1L)).thenReturn(null);
        assertThat(resolver.resolve(1L).active()).isFalse();
    }

    @Test
    @DisplayName("场景：已过期但库状态仍为 ACTIVE，惰性返回非会员并触发异步过期")
    void resolve_shouldReturnNoneAndAsyncExpire_whenExpired() {
        SysUserVipPO vip = new SysUserVipPO();
        vip.setUserId(1L);
        vip.setVipLevelId(1);
        vip.setStatus(VipUserStatusEnum.ACTIVE.getCode());
        vip.setExpireTime(LocalDateTime.now().minusHours(1));
        when(userVipMapper.selectById(1L)).thenReturn(vip);

        VipStatus status = resolver.resolve(1L);

        assertThat(status.active()).isFalse();
        verify(vipExpireService).markExpiredAsync(1L);
    }

    @Test
    @DisplayName("场景：有效 VIP，返回等级信息")
    void resolve_shouldReturnActive_whenValid() {
        SysUserVipPO vip = new SysUserVipPO();
        vip.setUserId(1L);
        vip.setVipLevelId(3);
        vip.setStatus(VipUserStatusEnum.ACTIVE.getCode());
        vip.setExpireTime(LocalDateTime.now().plusDays(10));
        when(userVipMapper.selectById(1L)).thenReturn(vip);

        SysVipLevelPO level = new SysVipLevelPO();
        level.setId(3);
        level.setLevelCode("GOLD");
        level.setLevelName("黄金会员");
        level.setWeight(30);
        when(vipLevelMapper.selectById(3)).thenReturn(level);

        VipStatus status = resolver.resolve(1L);

        assertThat(status.active()).isTrue();
        assertThat(status.levelCode()).isEqualTo("GOLD");
    }
}
