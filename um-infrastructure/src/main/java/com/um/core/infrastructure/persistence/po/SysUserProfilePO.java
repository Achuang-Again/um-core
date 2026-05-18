package com.um.core.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_profile")
public class SysUserProfilePO {

    @TableId
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthday;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private String extData;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
