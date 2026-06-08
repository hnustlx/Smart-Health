CREATE TABLE `plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `plan_type` varchar(30) NOT NULL DEFAULT 'COMBINED',
  `plan_level` varchar(20) NOT NULL DEFAULT 'BASIC',
  `plan_content` text NOT NULL,
  `trend_summary` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_user_create_time` (`user_id`, `create_time`),
  KEY `idx_plan_type` (`plan_type`),
  KEY `idx_plan_level` (`plan_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
