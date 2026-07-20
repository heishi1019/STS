-- MySQL 8.0+
-- Add bilingual display fields, biomedical entity index, and paper references.

USE `biomed_literature`;

SET @has_title_zh := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'paper'
      AND COLUMN_NAME = 'title_zh'
);
SET @sql := IF(
    @has_title_zh = 0,
    'ALTER TABLE `paper` ADD COLUMN `title_zh` VARCHAR(1000) NULL COMMENT ''Chinese translated title'' AFTER `title`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_abstract_zh := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'paper'
      AND COLUMN_NAME = 'abstract_zh'
);
SET @sql := IF(
    @has_abstract_zh = 0,
    'ALTER TABLE `paper` ADD COLUMN `abstract_zh` LONGTEXT NULL COMMENT ''Chinese translated abstract'' AFTER `abstract_text`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `biomedical_entity` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `entity_name` VARCHAR(255) NOT NULL COMMENT 'Disease, gene, drug, symptom, or keyword name',
    `entity_type` VARCHAR(32) NOT NULL COMMENT 'DISEASE, GENE, DRUG, SYMPTOM, or KEYWORD',
    `description` TEXT NULL COMMENT 'Optional entity description',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biomedical_entity_name_type` (`entity_name`, `entity_type`),
    KEY `idx_biomedical_entity_type_name` (`entity_type`, `entity_name`),
    CONSTRAINT `chk_biomedical_entity_type`
        CHECK (`entity_type` IN ('DISEASE', 'GENE', 'DRUG', 'SYMPTOM', 'KEYWORD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Biomedical knowledge entities';

CREATE TABLE IF NOT EXISTS `paper_entity` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Paper identifier',
    `entity_id` BIGINT UNSIGNED NOT NULL COMMENT 'Biomedical entity identifier',
    `source` VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT 'manual, rule, pubtator, or ai',
    `confidence` DECIMAL(5,4) NULL COMMENT 'Optional confidence between 0 and 1',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_entity` (`paper_id`, `entity_id`),
    KEY `idx_paper_entity_entity` (`entity_id`),
    CONSTRAINT `chk_paper_entity_source`
        CHECK (`source` IN ('manual', 'rule', 'pubtator', 'ai')),
    CONSTRAINT `chk_paper_entity_confidence`
        CHECK (`confidence` IS NULL OR `confidence` BETWEEN 0 AND 1),
    CONSTRAINT `fk_paper_entity_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_paper_entity_entity` FOREIGN KEY (`entity_id`) REFERENCES `biomedical_entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper-biomedical entity relationships';

CREATE TABLE IF NOT EXISTS `paper_reference` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paper_id` BIGINT UNSIGNED NOT NULL COMMENT 'Source paper identifier',
    `cited_paper_id` BIGINT UNSIGNED NULL COMMENT 'Local cited paper identifier, if collected',
    `cited_pmid` VARCHAR(32) NULL COMMENT 'Cited PubMed identifier',
    `cited_doi` VARCHAR(255) NULL COMMENT 'Cited DOI',
    `citation_text` TEXT NULL COMMENT 'Original citation text',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_reference_pmid` (`paper_id`, `cited_pmid`),
    KEY `idx_paper_reference_cited_paper` (`cited_paper_id`),
    KEY `idx_paper_reference_cited_doi` (`cited_doi`),
    CONSTRAINT `fk_paper_reference_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_paper_reference_cited_paper` FOREIGN KEY (`cited_paper_id`) REFERENCES `paper` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Paper reference and citation links';

INSERT IGNORE INTO `biomedical_entity` (`entity_name`, `entity_type`) VALUES
    ('diabetes', 'DISEASE'),
    ('cancer', 'DISEASE'),
    ('tumor', 'DISEASE'),
    ('tumour', 'DISEASE'),
    ('breast cancer', 'DISEASE'),
    ('lung cancer', 'DISEASE'),
    ('Alzheimer', 'DISEASE'),
    ('Alzheimer''s disease', 'DISEASE'),
    ('Parkinson', 'DISEASE'),
    ('COVID-19', 'DISEASE'),
    ('hypertension', 'DISEASE'),
    ('obesity', 'DISEASE'),
    ('TP53', 'GENE'),
    ('BRCA1', 'GENE'),
    ('BRCA2', 'GENE'),
    ('EGFR', 'GENE'),
    ('APOE', 'GENE'),
    ('TNF', 'GENE'),
    ('IL6', 'GENE'),
    ('metformin', 'DRUG'),
    ('insulin', 'DRUG'),
    ('aspirin', 'DRUG'),
    ('pembrolizumab', 'DRUG'),
    ('nivolumab', 'DRUG'),
    ('cisplatin', 'DRUG'),
    ('fever', 'SYMPTOM'),
    ('pain', 'SYMPTOM'),
    ('cough', 'SYMPTOM'),
    ('fatigue', 'SYMPTOM'),
    ('headache', 'SYMPTOM'),
    ('dyspnea', 'SYMPTOM');

INSERT IGNORE INTO `paper_entity` (`paper_id`, `entity_id`, `source`, `confidence`)
SELECT DISTINCT p.id, be.id, 'rule', 0.7000
FROM `paper` p
INNER JOIN `biomedical_entity` be
WHERE be.entity_type IN ('DISEASE', 'GENE', 'DRUG', 'SYMPTOM')
  AND LOWER(CONCAT_WS(' ', p.title, p.abstract_text)) LIKE CONCAT('%', LOWER(be.entity_name), '%');

INSERT IGNORE INTO `biomedical_entity` (`entity_name`, `entity_type`)
SELECT DISTINCT k.name, 'KEYWORD'
FROM `keyword` k
WHERE k.name IS NOT NULL AND k.name != '';

INSERT IGNORE INTO `paper_entity` (`paper_id`, `entity_id`, `source`, `confidence`)
SELECT DISTINCT pk.paper_id, be.id, 'rule', 0.7000
FROM `paper_keyword` pk
INNER JOIN `keyword` k ON k.id = pk.keyword_id
INNER JOIN `biomedical_entity` be
    ON be.entity_type = 'KEYWORD'
   AND be.entity_name = k.name;
