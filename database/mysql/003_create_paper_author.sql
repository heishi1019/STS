-- MySQL 8.0+
-- Link papers and authors while preserving author order and affiliation.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `paper_author` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Paper identifier',
    `author_id` BIGINT UNSIGNED NOT NULL COMMENT 'Author identifier',
    `author_order` SMALLINT UNSIGNED NOT NULL COMMENT 'One-based author order',
    `is_corresponding` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether the author is corresponding',
    `affiliation` TEXT NULL COMMENT 'Affiliation for this paper',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_author` (`paper_id`, `author_id`),
    UNIQUE KEY `uk_paper_author_order` (`paper_id`, `author_order`),
    KEY `idx_paper_author_author` (`author_id`),
    CONSTRAINT `chk_paper_author_order` CHECK (`author_order` > 0),
    CONSTRAINT `fk_paper_author_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_paper_author_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper-author relationships';