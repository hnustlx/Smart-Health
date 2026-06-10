-- Seed test users and AI configurations
-- Passwords:
--   admin2   / Admin@2026
--   viptest1 / Test12345
--   viptest2 / Test12345
--   user1    / Test12345
--   user2    / Test12345
--   user3    / Test12345
-- Insert only if not exists

-- 1 admin user
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('admin2', '$2b$12$D/gWps5y2ja2aa/E1hOnH.J93Nu877xnzWjoxVhVv4p5iKw9ErFia', 'ADMIN', 1, NOW(), NOW());

-- 2 VIP users
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('viptest1', '$2b$12$Vnvk9fl9iSl0VOyjCEkjL.5pAJWSY8i4ey6NHCz72d5p.gQlKd64i', 'VIP', 1, NOW(), NOW());

INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('viptest2', '$2b$12$Vnvk9fl9iSl0VOyjCEkjL.5pAJWSY8i4ey6NHCz72d5p.gQlKd64i', 'VIP', 1, NOW(), NOW());

-- 3 normal users
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('user1', '$2b$12$Vnvk9fl9iSl0VOyjCEkjL.5pAJWSY8i4ey6NHCz72d5p.gQlKd64i', 'USER', 1, NOW(), NOW());

INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('user2', '$2b$12$Vnvk9fl9iSl0VOyjCEkjL.5pAJWSY8i4ey6NHCz72d5p.gQlKd64i', 'USER', 1, NOW(), NOW());

INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('user3', '$2b$12$Vnvk9fl9iSl0VOyjCEkjL.5pAJWSY8i4ey6NHCz72d5p.gQlKd64i', 'USER', 1, NOW(), NOW());

-- AI config records for each test user (DEFAULT provider, no custom API key)
INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'admin2';

INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'viptest1';

INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'viptest2';

INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'user1';

INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'user2';

INSERT IGNORE INTO `user_ai_config` (`user_id`, `provider`, `custom_provider`, `created_at`, `updated_at`)
SELECT `id`, 'DEFAULT', 'deepseek', NOW(), NOW() FROM `user` WHERE `username` = 'user3';
