CREATE TABLE `daily_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `checkin_date` date NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_checkin_user_date` (`user_id`, `checkin_date`),
  KEY `idx_daily_checkin_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE `vip_activation_code`
  ADD COLUMN `vip_days` int NOT NULL DEFAULT 30 COMMENT '激活后增加的 VIP 天数',
  ADD COLUMN `source` varchar(30) NOT NULL DEFAULT 'ADMIN' COMMENT 'ADMIN=管理员生成 CHECKIN_REWARD=打卡奖励';
