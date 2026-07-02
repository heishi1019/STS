-- MySQL 8.0+
-- Track literature collection jobs and their progress.

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `crawl_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `task_name` VARCHAR(255) NOT NULL COMMENT 'Task display name',
    `data_source` VARCHAR(50) NOT NULL COMMENT 'pubmed, europe_pmc, semantic_scholar, etc.',
    `query_text` TEXT NOT NULL COMMENT 'Source query expression',
    `request_params` JSON NULL COMMENT 'Additional source request parameters',
    `task_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'Task lifecycle status',
    `cursor_token` TEXT NULL COMMENT 'Incremental cursor or continuation token',
    `total_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `processed_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `success_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `failed_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `error_message` TEXT NULL COMMENT 'Last failure message',
    `started_at` DATETIME(3) NULL,
    `finished_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_crawl_task_status_created` (`task_status`, `created_at`),
    KEY `idx_crawl_task_source` (`data_source`),
    CONSTRAINT `chk_crawl_task_status`
        CHECK (`task_status` IN ('pending', 'running', 'success', 'failed', 'cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Literature collection tasks';