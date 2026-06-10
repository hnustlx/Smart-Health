-- Add custom_provider column for multi-AI-provider support
ALTER TABLE `user_ai_config`
    ADD COLUMN `custom_provider` VARCHAR(20) DEFAULT 'deepseek'
    COMMENT 'deepseek / openai / claude' AFTER `provider`;
