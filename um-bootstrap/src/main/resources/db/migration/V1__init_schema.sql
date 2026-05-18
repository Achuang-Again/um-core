-- UM-Core schema (sys_* tables per project DDL)

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希值',
  `phone` varchar(20) DEFAULT NULL COMMENT '绑定手机号',
  `email` varchar(64) DEFAULT NULL COMMENT '绑定邮箱',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '业务状态: 0=禁用, 1=正常, 2=已注销',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `ext_data` json DEFAULT NULL COMMENT '扩展JSON',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统_用户账号表';

CREATE TABLE IF NOT EXISTS `sys_user_profile` (
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `nickname` varchar(64) NOT NULL DEFAULT '默认用户' COMMENT '昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) NOT NULL DEFAULT 0 COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `ext_data` json DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统_用户详细信息表';

CREATE TABLE IF NOT EXISTS `sys_vip_level` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `level_code` varchar(32) NOT NULL,
  `level_name` varchar(64) NOT NULL,
  `weight` int(11) NOT NULL,
  `monthly_price` decimal(10,2) NOT NULL,
  `is_active` tinyint(4) NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `ext_data` json DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_weight` (`weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP等级字典表';

CREATE TABLE IF NOT EXISTS `sys_user_vip` (
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `vip_level_id` int(11) UNSIGNED NOT NULL,
  `start_time` datetime NOT NULL,
  `expire_time` datetime NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1=生效,0=过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `ext_data` json DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户VIP状态表';

CREATE TABLE IF NOT EXISTS `sys_vip_order` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `order_no` varchar(64) NOT NULL,
  `action_type` tinyint(4) NOT NULL,
  `old_level_id` int(11) UNSIGNED DEFAULT NULL,
  `new_level_id` int(11) UNSIGNED NOT NULL,
  `duration_days` int(11) NOT NULL,
  `paid_amount` decimal(10,2) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `ext_data` json DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_userid_createtime` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP变更流水表';
