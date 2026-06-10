ALTER TABLE `user`
  ADD COLUMN `last_active_time` datetime DEFAULT NULL,
  ADD KEY `idx_user_last_active_time` (`last_active_time`);

CREATE TABLE `knowledge_reference_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `knowledge_id` varchar(100) NOT NULL,
  `knowledge_title` varchar(200) DEFAULT NULL,
  `knowledge_category` varchar(100) DEFAULT NULL,
  `source_type` varchar(30) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_reference_create_time` (`create_time`),
  KEY `idx_knowledge_reference_knowledge_id` (`knowledge_id`),
  KEY `idx_knowledge_reference_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
