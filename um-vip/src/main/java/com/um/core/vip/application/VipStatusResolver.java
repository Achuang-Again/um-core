package com.um.core.vip.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.um.core.domain.enums.VipUserStatusEnum;
import com.um.core.domain.model.vip.VipStatus;
import com.um.core.infrastructure.persistence.mapper.SysUserVipMapper;
import com.um.core.infrastructure.persistence.mapper.SysVipLevelMapper;
import com.um.core.infrastructure.persistence.po.SysUserVipPO;
import com.um.core.infrastructure.persistence.po.SysVipLevelPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * VIP 惰性求值：过期则对调用方返回非 VIP，并异步更新库状态。
 */
@Service
@RequiredArgsConstructor
public class VipStatusResolver {

    private final SysUserVipMapper userVipMapper;
    private final SysVipLevelMapper vipLevelMapper;
    private final VipExpireService vipExpireService;

    public VipStatus resolve(Long userId) {
        SysUserVipPO vip = userVipMapper.selectById(userId);
        if (vip == null) {
            return VipStatus.none();
        }
        LocalDateTime now = LocalDateTime.now();
        if (vip.getExpireTime() == null || vip.getExpireTime().isBefore(now)) {
            if (vip.getStatus() != null && vip.getStatus() == VipUserStatusEnum.ACTIVE.getCode()) {
                vipExpireService.markExpiredAsync(userId);
            }
            return VipStatus.none();
        }
        if (vip.getStatus() != null && vip.getStatus() == VipUserStatusEnum.EXPIRED.getCode()) {
            return VipStatus.none();
        }
        SysVipLevelPO level = vipLevelMapper.selectById(vip.getVipLevelId());
        if (level == null) {
            return VipStatus.none();
        }
        return new VipStatus(true, level.getId(), level.getLevelCode(), level.getLevelName(),
                level.getWeight(), vip.getExpireTime());
    }

    public SysVipLevelPO requireLevel(String levelCode) {
        SysVipLevelPO level = vipLevelMapper.selectOne(new LambdaQueryWrapper<SysVipLevelPO>()
                .eq(SysVipLevelPO::getLevelCode, levelCode)
                .eq(SysVipLevelPO::getIsActive, 1));
        if (level == null) {
            return null;
        }
        return level;
    }

    public SysVipLevelPO requireLevelById(Integer levelId) {
        if (levelId == null) {
            return null;
        }
        return vipLevelMapper.selectById(levelId);
    }
}
