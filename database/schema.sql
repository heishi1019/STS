-- biomed_literature official full initialization schema
-- Database: MySQL 8.4+
-- This file is the canonical initialization entry point.
-- Keep it synchronized whenever a numbered migration changes the structure.

SET NAMES utf8mb4;
-- ============================================================================
-- Source: database/mysql/001_create_database_and_paper.sql
-- ============================================================================
-- MySQL 8.0+
-- Create the project database and the first literature table.

CREATE DATABASE IF NOT EXISTS `biomed_literature`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `biomed_literature`;

CREATE TABLE IF NOT EXISTS `paper` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `title` VARCHAR(1000) NOT NULL COMMENT 'Paper title',
    `abstract_text` LONGTEXT NULL COMMENT 'Paper abstract',
    `journal` VARCHAR(500) NULL COMMENT 'Journal name',
    `publication_year` SMALLINT UNSIGNED NULL COMMENT 'Publication year',
    `doi` VARCHAR(255) NULL COMMENT 'Digital Object Identifier',
    `pmid` VARCHAR(32) NULL COMMENT 'PubMed identifier',
    `pmcid` VARCHAR(32) NULL COMMENT 'PubMed Central identifier',
    `data_source` VARCHAR(50) NOT NULL DEFAULT 'pubmed' COMMENT 'Metadata source',
    `pdf_url` VARCHAR(2048) NULL COMMENT 'PDF address',
    `full_text_url` VARCHAR(2048) NULL COMMENT 'Full-text address',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_doi` (`doi`),
    UNIQUE KEY `uk_paper_pmid` (`pmid`),
    UNIQUE KEY `uk_paper_pmcid` (`pmcid`),
    KEY `idx_paper_publication_year` (`publication_year`),
    KEY `idx_paper_journal` (`journal`),
    KEY `idx_paper_data_source` (`data_source`),
    CONSTRAINT `chk_paper_publication_year`
        CHECK (`publication_year` IS NULL OR `publication_year` BETWEEN 1500 AND 2100)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='Biomedical literature metadata';

-- ============================================================================
-- Source: database/mysql/002_create_author.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/003_create_paper_author.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/004_create_keyword.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/005_create_paper_keyword.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/006_create_tag.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/007_create_paper_tag.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/008_create_topic.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/009_create_topic_paper.sql
-- ============================================================================
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

-- ============================================================================
-- Source: database/mysql/010_create_crawl_task.sql
-- ============================================================================
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
