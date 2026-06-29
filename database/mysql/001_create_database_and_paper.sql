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