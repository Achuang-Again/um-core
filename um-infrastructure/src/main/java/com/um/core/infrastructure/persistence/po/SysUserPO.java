package com.um.core.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUserPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private String extData;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
