-- MySQL 8.0+
-- Store author keywords, MeSH terms, and automatically extracted terms.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `keyword` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(255) NOT NULL COMMENT 'Display value',
    `normalized_name` VARCHAR(255) NOT NULL COMMENT 'Normalized value used for matching',
    `keyword_type` VARCHAR(32) NOT NULL DEFAULT 'author' COMMENT 'author, mesh, or extracted',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword_name_type` (`normalized_name`, `keyword_type`),
    KEY `idx_keyword_name` (`name`),
    CONSTRAINT `chk_keyword_type` CHECK (`keyword_type` IN ('author', 'mesh', 'extracted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper keywords';