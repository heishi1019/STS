-- MySQL 8.0+
-- Link topics and papers, recording how the paper was added.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `topic_paper` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `topic_id` BIGINT UNSIGNED NOT NULL COMMENT 'Topic identifier',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Paper identifier',
    `added_by` VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT 'manual, rule, or ai',
    `relevance_score` DECIMAL(5,4) NULL COMMENT 'Optional score between 0 and 1',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_paper` (`topic_id`, `paper_id`),
    KEY `idx_topic_paper_paper` (`paper_id`),
    CONSTRAINT `chk_topic_paper_added_by` CHECK (`added_by` IN ('manual', 'rule', 'ai')),
    CONSTRAINT `chk_topic_paper_score` CHECK (`relevance_score` IS NULL OR `relevance_score` BETWEEN 0 AND 1),
    CONSTRAINT `fk_topic_paper_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_topic_paper_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Topic-paper relationships';