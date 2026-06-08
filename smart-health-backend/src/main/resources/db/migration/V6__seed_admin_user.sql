-- Seed default admin user (password: Admin@2026)
-- WARNING: Change this password immediately after first login in production
INSERT IGNORE INTO user (username, password, role, status, create_time, update_time)
VALUES ('admin', '$2a$12$LJ3m4ys3Lg3YOXyF0P.Ja.Lq4RFfRF5D5.4Ej5f5G5o5H5i5J5K5O', 'ADMIN', 1, NOW(), NOW());
