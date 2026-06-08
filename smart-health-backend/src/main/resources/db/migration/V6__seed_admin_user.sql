-- Seed default admin user (password: admin123)
-- The password is BCrypt encoded with cost factor 12
INSERT IGNORE INTO user (username, password, role, status, create_time, update_time)
VALUES ('admin', '$2b$12$aMfxHZe4567WpiL6FxS4NODAGvefykzI6U8aUPBuSLRyWySvyXuI.', 'ADMIN', 1, NOW(), NOW());
