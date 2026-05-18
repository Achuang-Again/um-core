INSERT INTO sys_vip_level (level_code, level_name, weight, monthly_price, is_active, create_by, update_by)
VALUES
    ('BRONZE', '青铜会员', 10, 19.90, 1, 'system', 'system'),
    ('SILVER', '白银会员', 20, 39.90, 1, 'system', 'system'),
    ('GOLD', '黄金会员', 30, 79.90, 1, 'system', 'system')
ON DUPLICATE KEY UPDATE level_name = VALUES(level_name);
