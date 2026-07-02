-- MySQL 8.0+
-- Create the biomedical literature author table.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `author` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `full_name` VARCHAR(500) NOT NULL COMMENT 'Display name',
    `last_name` VARCHAR(255) NULL COMMENT 'Family name',
    `fore_name` VARCHAR(255) NULL COMMENT 'Given name',
    `initials` VARCHAR(50) NULL COMMENT 'Author initials',
    `collective_name` VARCHAR(500) NULL COMMENT 'Consortium or group author name',
    `normalized_name` VARCHAR(500) NOT NULL COMMENT 'Normalized name used for matching',
    `orcid` VARCHAR(32) NULL COMMENT 'Normalized ORCID, for example 0000-0000-0000-0000',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_author_orcid` (`orcid`),
    KEY `idx_author_normalized_name` (`normalized_name`),
    KEY `idx_author_last_name` (`last_name`),
    CONSTRAINT `chk_author_orcid`
        CHECK (`orcid` IS NULL OR `orcid` REGEXP '^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9X]$')
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='Biomedical literature authors';