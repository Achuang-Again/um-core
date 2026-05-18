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
@TableName("sys_vip_order")
public class SysVipOrderPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String orderNo;
    private Integer actionType;
    private Integer oldLevelId;
    private Integer newLevelId;
    private Integer durationDays;
    private BigDecimal paidAmount;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private String extData;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
