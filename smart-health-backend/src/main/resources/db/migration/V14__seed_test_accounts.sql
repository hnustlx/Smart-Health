-- Seed test accounts for development and testing
-- Passwords:
--   testuser / Test12345
--   viptest / Test12345
-- Insert only if not exists

INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('testuser', '$2b$12$rvCN59zHnate4ba0l2T3r.qe.SHxOpT8vDvMpTe2X/1Ly14wOLnmK', 'USER', 1, NOW(), NOW());

INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`, `create_time`, `update_time`)
VALUES ('viptest', '$2b$12$rvCN59zHnate4ba0l2T3r.qe.SHxOpT8vDvMpTe2X/1Ly14wOLnmK', 'VIP', 1, NOW(), NOW());

-- Seed test VIP activation codes
-- Valid for 30 days from insertion

INSERT IGNORE INTO `vip_activation_code` (`code`, `status`, `created_by`, `created_at`, `expires_at`)
VALUES ('VIP-TEST0001', 0, (SELECT `id` FROM `user` WHERE `username` = 'admin' LIMIT 1), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

INSERT IGNORE INTO `vip_activation_code` (`code`, `status`, `created_by`, `created_at`, `expires_at`)
VALUES ('VIP-TEST0002', 0, (SELECT `id` FROM `user` WHERE `username` = 'admin' LIMIT 1), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));
