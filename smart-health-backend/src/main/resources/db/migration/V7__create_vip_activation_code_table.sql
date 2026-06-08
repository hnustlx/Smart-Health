CREATE TABLE IF NOT EXISTS `vip_activation_code` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(32) NOT NULL UNIQUE COMMENT '激活码',
    `status` TINYINT DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
    `used_by` BIGINT DEFAULT NULL COMMENT '使用的用户ID',
    `created_by` BIGINT NOT NULL COMMENT '生成的管理员ID',
    `created_at` DATETIME NOT NULL,
    `expires_at` DATETIME NOT NULL COMMENT '过期时间 = created_at + 7天',
    `used_at` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP激活码';
