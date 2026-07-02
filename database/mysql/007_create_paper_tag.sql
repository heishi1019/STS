-- MySQL 8.0+
-- Link papers and tags.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `paper_tag` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Paper identifier',
    `tag_id` BIGINT UNSIGNED NOT NULL COMMENT 'Tag identifier',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_tag` (`paper_id`, `tag_id`),
    KEY `idx_paper_tag_tag` (`tag_id`),
    CONSTRAINT `fk_paper_tag_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_paper_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper-tag relationships';