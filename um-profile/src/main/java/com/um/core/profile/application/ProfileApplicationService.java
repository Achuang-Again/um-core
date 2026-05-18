package com.um.core.profile.application;

import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.exception.BusinessException;
import com.um.core.infrastructure.persistence.mapper.SysUserProfileMapper;
import com.um.core.infrastructure.persistence.po.SysUserProfilePO;
import com.um.core.infrastructure.util.JsonExtHelper;
import com.um.core.profile.application.command.UpdateProfileCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户资料：初始化、查询、更新（含 extension JSON 合并）。
 */
@Service
@RequiredArgsConstructor
public class ProfileApplicationService {

    private final SysUserProfileMapper profileMapper;

    @Transactional(rollbackFor = Exception.class)
    public void initProfile(Long userId, String nickname) {
        SysUserProfilePO po = new SysUserProfilePO();
        po.setUserId(userId);
        po.setNickname(nickname != null ? nickname : "用户" + String.valueOf(userId).substring(Math.max(0, String.valueOf(userId).length() - 4)));
        po.setGender(0);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        po.setCreateBy("system");
        po.setUpdateBy("system");
        profileMapper.insert(po);
    }

    @Transactional(readOnly = true)
    public SysUserProfilePO getProfile(Long userId) {
        SysUserProfilePO po = profileMapper.selectById(userId);
        if (po == null) {
            throw new BusinessException(ErrorCodes.PROFILE_NOT_FOUND, "用户资料不存在");
        }
        return po;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUserProfilePO updateProfile(UpdateProfileCommand cmd) {
        SysUserProfilePO po = getProfile(cmd.userId());
        if (cmd.nickname() != null) {
            po.setNickname(cmd.nickname());
        }
        if (cmd.avatarUrl() != null) {
            po.setAvatarUrl(cmd.avatarUrl());
        }
        if (cmd.gender() != null) {
            po.setGender(cmd.gender());
        }
        if (cmd.birthday() != null) {
            po.setBirthday(cmd.birthday());
        }
        if (cmd.extPatch() != null && !cmd.extPatch().isEmpty()) {
            Map<String, Object> current = JsonExtHelper.parse(po.getExtData());
            cmd.extPatch().forEach((k, v) -> {
                if (!k.startsWith("_")) {
                    current.put(k, v);
                }
            });
            po.setExtData(JsonExtHelper.toJson(current));
        }
        po.setUpdateTime(LocalDateTime.now());
        po.setUpdateBy(String.valueOf(cmd.userId()));
        profileMapper.updateById(po);
        return po;
    }
}
