-- PostgreSQL 15+ first-pass schema.
BEGIN;

CREATE TABLE paper (
    id BIGSERIAL PRIMARY KEY,
    pmid VARCHAR(16), pmcid VARCHAR(32), doi VARCHAR(255),
    title TEXT NOT NULL, abstract TEXT,
    publication_year SMALLINT CHECK (publication_year IS NULL OR publication_year BETWEEN 1500 AND 2100),
    publication_date DATE, journal_name VARCHAR(500), journal_iso_abbreviation VARCHAR(100),
    issn VARCHAR(16), language VARCHAR(32), source VARCHAR(32) NOT NULL,
    source_record_id VARCHAR(255), pdf_url TEXT, content_hash CHAR(64), raw_payload JSONB,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', COALESCE(title, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(abstract, '')), 'B')
    ) STORED,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_paper_pmid ON paper (pmid) WHERE pmid IS NOT NULL;
CREATE UNIQUE INDEX uq_paper_doi ON paper (LOWER(doi)) WHERE doi IS NOT NULL;
CREATE UNIQUE INDEX uq_paper_source_id ON paper (source, source_record_id) WHERE source_record_id IS NOT NULL;
CREATE INDEX idx_paper_search ON paper USING GIN (search_vector);
CREATE INDEX idx_paper_year ON paper (publication_year);

CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY, full_name VARCHAR(500) NOT NULL,
    last_name VARCHAR(255), fore_name VARCHAR(255), initials VARCHAR(50),
    orcid VARCHAR(32), normalized_name VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_author_orcid ON author (LOWER(orcid)) WHERE orcid IS NOT NULL;
CREATE INDEX idx_author_name ON author (normalized_name);

CREATE TABLE keyword (
    id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL UNIQUE,
    keyword_type VARCHAR(32) NOT NULL DEFAULT 'author' CHECK (keyword_type IN ('author','mesh','extracted')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE topic (
    id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE, description TEXT, search_query TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(7) CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$'),
    description TEXT, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE paper_author (
    paper_id BIGINT REFERENCES paper(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES author(id) ON DELETE RESTRICT,
    author_order SMALLINT NOT NULL CHECK (author_order > 0),
    is_corresponding BOOLEAN NOT NULL DEFAULT FALSE, affiliation TEXT,
    PRIMARY KEY (paper_id, author_id), UNIQUE (paper_id, author_order)
);
CREATE INDEX idx_pa_author ON paper_author (author_id);
CREATE TABLE paper_keyword (
    paper_id BIGINT REFERENCES paper(id) ON DELETE CASCADE,
    keyword_id BIGINT REFERENCES keyword(id) ON DELETE CASCADE,
    PRIMARY KEY (paper_id, keyword_id)
);
CREATE INDEX idx_pk_keyword ON paper_keyword (keyword_id);
CREATE TABLE paper_topic (
    paper_id BIGINT REFERENCES paper(id) ON DELETE CASCADE,
    topic_id BIGINT REFERENCES topic(id) ON DELETE CASCADE,
    added_by VARCHAR(32) NOT NULL DEFAULT 'manual' CHECK (added_by IN ('manual','rule','ai')),
    relevance_score NUMERIC(5,4) CHECK (relevance_score IS NULL OR relevance_score BETWEEN 0 AND 1),
    PRIMARY KEY (paper_id, topic_id)
);
CREATE INDEX idx_pt_topic ON paper_topic (topic_id);
CREATE TABLE paper_tag (
    paper_id BIGINT REFERENCES paper(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (paper_id, tag_id)
);
CREATE INDEX idx_pTag_tag ON paper_tag (tag_id);

INSERT INTO topic (name, slug, description, search_query) VALUES
('糖尿病相关研究','diabetes-research','糖尿病诊疗、并发症及基础研究。','diabetes mellitus[MeSH Terms]'),
('肿瘤免疫治疗','cancer-immunotherapy','肿瘤免疫机制与治疗研究。','neoplasms[MeSH Terms] AND immunotherapy[MeSH Terms]'),
('阿尔茨海默病','alzheimers-disease','病理、诊断、生物标志物与治疗。','Alzheimer Disease[MeSH Terms]');
COMMIT;
