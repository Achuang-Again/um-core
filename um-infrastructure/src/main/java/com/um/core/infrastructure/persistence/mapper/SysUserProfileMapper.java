package com.um.core.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.um.core.infrastructure.persistence.po.SysUserProfilePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserProfileMapper extends BaseMapper<SysUserProfilePO> {
}
