package com.um.core.vip.application;

import com.um.core.domain.enums.VipUserStatusEnum;
import com.um.core.infrastructure.persistence.mapper.SysUserVipMapper;
import com.um.core.infrastructure.persistence.po.SysUserVipPO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 惰性过期后的异步落库，禁止 Cron 全表扫描。
 */
@Service
@RequiredArgsConstructor
public class VipExpireService {

    private static final Logger log = LoggerFactory.getLogger(VipExpireService.class);

    private final SysUserVipMapper userVipMapper;

    @Async("umTaskExecutor")
    public void markExpiredAsync(Long userId) {
        SysUserVipPO po = userVipMapper.selectById(userId);
        if (po == null) {
            return;
        }
        if (po.getExpireTime() != null && po.getExpireTime().isBefore(LocalDateTime.now())) {
            po.setStatus(VipUserStatusEnum.EXPIRED.getCode());
            po.setUpdateTime(LocalDateTime.now());
            userVipMapper.updateById(po);
            log.info("VIP marked expired async userId={}", userId);
        }
    }
}
