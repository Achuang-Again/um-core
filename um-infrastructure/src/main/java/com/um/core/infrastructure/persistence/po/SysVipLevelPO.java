package com.um.core.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_vip_level")
public class SysVipLevelPO {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String levelCode;
    private String levelName;
    private Integer weight;
    private BigDecimal monthlyPrice;
    private Integer isActive;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private String extData;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
