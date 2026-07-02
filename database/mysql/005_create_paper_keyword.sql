-- MySQL 8.0+
-- Link papers and keywords.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `paper_keyword` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Paper identifier',
    `keyword_id` BIGINT UNSIGNED NOT NULL COMMENT 'Keyword identifier',
    `is_major_topic` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'MeSH major-topic marker',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_keyword` (`paper_id`, `keyword_id`),
    KEY `idx_paper_keyword_keyword` (`keyword_id`),
    CONSTRAINT `fk_paper_keyword_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_paper_keyword_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper-keyword relationships';