-- MySQL 8.0+
-- Store literature topics and their optional saved search query.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `topic` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(255) NOT NULL COMMENT 'Topic name',
    `slug` VARCHAR(255) NOT NULL COMMENT 'URL-safe identifier',
    `description` TEXT NULL COMMENT 'Topic description',
    `search_query` TEXT NULL COMMENT 'Saved source search expression',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_name` (`name`),
    UNIQUE KEY `uk_topic_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Literature topics';