package com.um.core.vip.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.exception.BusinessException;
import com.um.core.domain.enums.VipActionTypeEnum;
import com.um.core.domain.enums.VipUserStatusEnum;
import com.um.core.domain.model.vip.VipStatus;
import com.um.core.infrastructure.persistence.mapper.SysUserVipMapper;
import com.um.core.infrastructure.persistence.mapper.SysVipOrderMapper;
import com.um.core.infrastructure.persistence.po.SysUserVipPO;
import com.um.core.infrastructure.persistence.po.SysVipLevelPO;
import com.um.core.infrastructure.persistence.po.SysVipOrderPO;
import com.um.core.infrastructure.redis.IdempotencyService;
import com.um.core.vip.application.command.VipSubscribeCommand;
import com.um.core.vip.domain.VipUpgradeCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VipApplicationService {

    private static final Logger log = LoggerFactory.getLogger(VipApplicationService.class);

    private final SysUserVipMapper userVipMapper;
    private final SysVipOrderMapper vipOrderMapper;
    private final VipStatusResolver vipStatusResolver;
    private final IdempotencyService idempotencyService;

    @Transactional(rollbackFor = Exception.class)
    public VipStatus subscribe(VipSubscribeCommand cmd) {
        assertIdempotent(cmd.idempotencyKey(), cmd.orderNo());
        SysVipLevelPO level = requireLevel(cmd.levelCode());
        assertOrderUnique(cmd.orderNo());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expire = now.plusDays(cmd.durationDays());

        SysUserVipPO existing = userVipMapper.selectById(cmd.userId());
        if (existing == null) {
            SysUserVipPO po = new SysUserVipPO();
            po.setUserId(cmd.userId());
            po.setVipLevelId(level.getId());
            po.setStartTime(now);
            po.setExpireTime(expire);
            po.setStatus(VipUserStatusEnum.ACTIVE.getCode());
            po.setCreateTime(now);
            po.setUpdateTime(now);
            po.setCreateBy("vip");
            po.setUpdateBy("vip");
            userVipMapper.insert(po);
        } else {
            existing.setVipLevelId(level.getId());
            existing.setStartTime(now);
            existing.setExpireTime(expire);
            existing.setStatus(VipUserStatusEnum.ACTIVE.getCode());
            existing.setUpdateTime(now);
            userVipMapper.updateById(existing);
        }
        saveOrder(cmd.userId(), cmd.orderNo(), VipActionTypeEnum.SUBSCRIBE, null, level.getId(),
                cmd.durationDays(), cmd.paidAmount());
        log.info("VIP subscribe userId={} level={}", cmd.userId(), cmd.levelCode());
        return vipStatusResolver.resolve(cmd.userId());
    }

    @Transactional(rollbackFor = Exception.class)
    public VipStatus renew(VipSubscribeCommand cmd) {
        assertIdempotent(cmd.idempotencyKey(), cmd.orderNo());
        SysVipLevelPO level = requireLevel(cmd.levelCode());
        assertOrderUnique(cmd.orderNo());

        SysUserVipPO vip = userVipMapper.selectById(cmd.userId());
        LocalDateTime now = LocalDateTime.now();
        Integer oldLevelId = vip != null ? vip.getVipLevelId() : null;
        LocalDateTime base = (vip == null || vip.getExpireTime() == null || vip.getExpireTime().isBefore(now))
                ? now : vip.getExpireTime();
        LocalDateTime expire = base.plusDays(cmd.durationDays());

        if (vip == null) {
            vip = new SysUserVipPO();
            vip.setUserId(cmd.userId());
            vip.setVipLevelId(level.getId());
            vip.setStartTime(now);
            vip.setCreateTime(now);
            vip.setCreateBy("vip");
        }
        vip.setVipLevelId(level.getId());
        vip.setExpireTime(expire);
        vip.setStatus(VipUserStatusEnum.ACTIVE.getCode());
        vip.setUpdateTime(now);
        vip.setUpdateBy("vip");
        if (vip.getStartTime() == null) {
            vip.setStartTime(now);
        }
        if (userVipMapper.selectById(cmd.userId()) == null) {
            userVipMapper.insert(vip);
        } else {
            userVipMapper.updateById(vip);
        }
        saveOrder(cmd.userId(), cmd.orderNo(), VipActionTypeEnum.RENEW, oldLevelId, level.getId(),
                cmd.durationDays(), cmd.paidAmount());
        return vipStatusResolver.resolve(cmd.userId());
    }

    @Transactional(rollbackFor = Exception.class)
    public VipStatus upgrade(Long userId, String newLevelCode, int purchasedDays,
                           String orderNo, BigDecimal paidAmount, String idempotencyKey) {
        assertIdempotent(idempotencyKey, orderNo);
        SysVipLevelPO newLevel = requireLevel(newLevelCode);
        assertOrderUnique(orderNo);

        SysUserVipPO vip = userVipMapper.selectById(userId);
        if (vip == null) {
            throw new BusinessException(ErrorCodes.VIP_INVALID_UPGRADE, "当前无 VIP，请使用开通接口");
        }
        SysVipLevelPO oldLevel = vipStatusResolver.requireLevelById(vip.getVipLevelId());
        if (oldLevel == null) {
            throw new BusinessException(ErrorCodes.VIP_LEVEL_NOT_FOUND, "原等级不存在");
        }
        if (newLevel.getWeight() <= oldLevel.getWeight()) {
            throw new BusinessException(ErrorCodes.VIP_INVALID_UPGRADE, "仅允许升级到更高权重等级");
        }

        LocalDateTime newExpire = VipUpgradeCalculator.calculateNewExpire(
                vip.getExpireTime(), oldLevel, newLevel, purchasedDays);
        Integer oldLevelId = vip.getVipLevelId();
        vip.setVipLevelId(newLevel.getId());
        vip.setExpireTime(newExpire);
        vip.setStatus(VipUserStatusEnum.ACTIVE.getCode());
        vip.setUpdateTime(LocalDateTime.now());
        userVipMapper.updateById(vip);

        saveOrder(userId, orderNo, VipActionTypeEnum.UPGRADE, oldLevelId, newLevel.getId(),
                purchasedDays, paidAmount);
        log.info("VIP upgrade userId={} {} -> {}", userId, oldLevel.getLevelCode(), newLevelCode);
        return vipStatusResolver.resolve(userId);
    }

    @Transactional(readOnly = true)
    public VipStatus getMyVip(Long userId) {
        return vipStatusResolver.resolve(userId);
    }

    private SysVipLevelPO requireLevel(String code) {
        SysVipLevelPO level = vipStatusResolver.requireLevel(code);
        if (level == null) {
            throw new BusinessException(ErrorCodes.VIP_LEVEL_NOT_FOUND, "VIP 等级不存在");
        }
        return level;
    }

    private void assertOrderUnique(String orderNo) {
        long count = vipOrderMapper.selectCount(new LambdaQueryWrapper<SysVipOrderPO>()
                .eq(SysVipOrderPO::getOrderNo, orderNo));
        if (count > 0) {
            throw new BusinessException(ErrorCodes.VIP_ORDER_DUPLICATE, "订单号已处理");
        }
    }

    private void assertIdempotent(String idempotencyKey, String orderNo) {
        String key = idempotencyKey != null ? idempotencyKey : orderNo;
        if (key != null && !idempotencyService.tryAcquire(key)) {
            throw new BusinessException(ErrorCodes.VIP_ORDER_DUPLICATE, "重复请求");
        }
    }

    private void saveOrder(Long userId, String orderNo, VipActionTypeEnum action,
                           Integer oldLevelId, Integer newLevelId, int days, BigDecimal amount) {
        SysVipOrderPO order = new SysVipOrderPO();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setActionType(action.getCode());
        order.setOldLevelId(oldLevelId);
        order.setNewLevelId(newLevelId);
        order.setDurationDays(days);
        order.setPaidAmount(amount != null ? amount : BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setCreateBy("vip");
        order.setUpdateBy("vip");
        vipOrderMapper.insert(order);
    }
}
