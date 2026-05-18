package com.um.core.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.um.core.domain.enums.UserStatusEnum;
import com.um.core.infrastructure.persistence.mapper.SysUserMapper;
import com.um.core.infrastructure.persistence.po.SysUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SysUserMapper userMapper;

    public Optional<SysUserPO> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    public Optional<SysUserPO> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<SysUserPO>()
                .eq(SysUserPO::getUsername, username)));
    }

    public Optional<SysUserPO> findByPhone(String phone) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<SysUserPO>()
                .eq(SysUserPO::getPhone, phone)));
    }

    public Optional<SysUserPO> findByEmail(String email) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<SysUserPO>()
                .eq(SysUserPO::getEmail, email)));
    }

    public void save(SysUserPO user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
    }

    public boolean existsUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUserPO>()
                .eq(SysUserPO::getUsername, username)) > 0;
    }

    public boolean existsPhone(String phone) {
        return phone != null && userMapper.selectCount(new LambdaQueryWrapper<SysUserPO>()
                .eq(SysUserPO::getPhone, phone)) > 0;
    }

    public boolean isActive(SysUserPO user) {
        return user != null && user.getStatus() != null
                && user.getStatus() == UserStatusEnum.NORMAL.getCode();
    }
}
