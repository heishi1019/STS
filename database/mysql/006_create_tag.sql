-- MySQL 8.0+
-- Store user-managed labels.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(100) NOT NULL COMMENT 'Tag name',
    `color` CHAR(7) NULL COMMENT 'Hex color such as #2563EB',
    `description` TEXT NULL COMMENT 'Tag description',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`name`),
    CONSTRAINT `chk_tag_color` CHECK (`color` IS NULL OR `color` REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper tags';