"""Transactional persistence for PubMed records."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from datetime import datetime
from typing import Any

import mysql.connector
from mysql.connector import MySQLConnection

from pubmed_client import PubMedClient, PubMedSearchResult


ENTITY_DICTIONARY: dict[str, list[str]] = {
    "DISEASE": [
        "diabetes",
        "cancer",
        "tumor",
        "tumour",
        "breast cancer",
        "lung cancer",
        "Alzheimer",
        "Alzheimer's disease",
        "Parkinson",
        "COVID-19",
        "hypertension",
        "obesity",
    ],
    "GENE": ["TP53", "BRCA1", "BRCA2", "EGFR", "APOE", "TNF", "IL6"],
    "DRUG": [
        "metformin",
        "insulin",
        "aspirin",
        "pembrolizumab",
        "nivolumab",
        "cisplatin",
    ],
    "SYMPTOM": ["fever", "pain", "cough", "fatigue", "headache", "dyspnea"],
}


@dataclass(frozen=True)
class DatabaseConfig:
    host: str = "127.0.0.1"
    port: int = 3306
    database: str = "biomed_literature"
    username: str = "root"
    password: str = ""


class PubMedMySQLCollector:
    def __init__(self, config: DatabaseConfig, client: PubMedClient) -> None:
        self.client = client
        self.connection: MySQLConnection = mysql.connector.connect(
            host=config.host,
            port=config.port,
            database=config.database,
            user=config.username,
            password=config.password,
            charset="utf8mb4",
            collation="utf8mb4_0900_ai_ci",
            autocommit=False,
        )

    def close(self) -> None:
        if self.connection.is_connected():
            self.connection.close()

    def collect(self, query: str, retmax: int) -> dict[str, Any]:
        task_id = self._create_task(query, retmax)
        try:
            result = self.client.fetch(query, retmax)
            self._set_total(task_id, result)
            return self._save_result(task_id, result)
        except Exception as error:
            self.connection.rollback()
            self._fail_task(task_id, str(error))
            raise

    def _create_task(self, query: str, retmax: int) -> int:
        cursor = self.connection.cursor()
        try:
            cursor.execute(
                """
                INSERT INTO crawl_task (
                    task_name, data_source, query_text, request_params,
                    task_status, total_count, processed_count,
                    success_count, failed_count, started_at
                )
                VALUES (%s, 'pubmed', %s, %s, 'running', 0, 0, 0, 0, %s)
                """,
                (
                    f"PubMed: {query[:180]}",
                    query,
                    json.dumps({"retmax": retmax}, ensure_ascii=False),
                    datetime.now(),
                ),
            )
            task_id = int(cursor.lastrowid)
            self.connection.commit()
            return task_id
        finally:
            cursor.close()

    def _set_total(self, task_id: int, result: PubMedSearchResult) -> None:
        cursor = self.connection.cursor()
        try:
            cursor.execute(
                "UPDATE crawl_task SET total_count = %s WHERE id = %s",
                (result.total_results, task_id),
            )
            self.connection.commit()
        finally:
            cursor.close()

    def _save_result(
        self, task_id: int, result: PubMedSearchResult
    ) -> dict[str, Any]:
        inserted = 0
        updated = 0
        errors: list[str] = []

        for paper in result.papers:
            try:
                outcome = self._save_paper(paper)
                self.connection.commit()
                inserted += int(outcome == "inserted")
                updated += int(outcome == "updated")
            except Exception as error:
                self.connection.rollback()
                errors.append(f"PMID {paper.get('pmid') or 'unknown'}: {error}")

        failed = len(errors)
        processed = len(result.papers)
        status = "success" if failed == 0 else "failed"
        cursor = self.connection.cursor()
        try:
            cursor.execute(
                """
                UPDATE crawl_task
                SET task_status = %s,
                    processed_count = %s,
                    success_count = %s,
                    failed_count = %s,
                    error_message = %s,
                    finished_at = %s
                WHERE id = %s
                """,
                (
                    status,
                    processed,
                    inserted + updated,
                    failed,
                    "\n".join(errors[:20]) or None,
                    datetime.now(),
                    task_id,
                ),
            )
            self.connection.commit()
        finally:
            cursor.close()

        return {
            "task_id": task_id,
            "query": result.query,
            "total_results": result.total_results,
            "fetched": processed,
            "inserted": inserted,
            "updated": updated,
            "failed": failed,
            "status": status,
        }

    def _save_paper(self, paper: dict[str, Any]) -> str:
        if not paper.get("title"):
            raise ValueError("标题为空")
        cursor = self.connection.cursor(dictionary=True)
        try:
            cursor.execute(
                """
                SELECT id
                FROM paper
                WHERE (%s IS NOT NULL AND pmid = %s)
                   OR (%s IS NOT NULL AND doi = %s)
                ORDER BY (pmid = %s) DESC
                LIMIT 1
                """,
                (
                    paper.get("pmid"),
                    paper.get("pmid"),
                    paper.get("doi"),
                    paper.get("doi"),
                    paper.get("pmid"),
                ),
            )
            existing = cursor.fetchone()
            values = self._paper_values(paper)
            if existing:
                paper_id = int(existing["id"])
                cursor.execute(
                    """
                    UPDATE paper
                    SET title = %s,
                        abstract_text = %s,
                        journal = %s,
                        publication_year = %s,
                        doi = COALESCE(%s, doi),
                        pmid = COALESCE(%s, pmid),
                        pmcid = COALESCE(%s, pmcid),
                        data_source = 'pubmed',
                        pdf_url = COALESCE(%s, pdf_url),
                        full_text_url = COALESCE(%s, full_text_url)
                    WHERE id = %s
                    """,
                    (*values, paper_id),
                )
                outcome = "updated"
            else:
                cursor.execute(
                    """
                    INSERT INTO paper (
                        title, abstract_text, journal, publication_year,
                        doi, pmid, pmcid, data_source, pdf_url, full_text_url
                    )
                    VALUES (%s, %s, %s, %s, %s, %s, %s, 'pubmed', %s, %s)
                    """,
                    values,
                )
                paper_id = int(cursor.lastrowid)
                outcome = "inserted"

            cursor.execute(
                "DELETE FROM paper_author WHERE paper_id = %s", (paper_id,)
            )
            cursor.execute(
                "DELETE FROM paper_keyword WHERE paper_id = %s", (paper_id,)
            )
            cursor.execute(
                "DELETE FROM paper_entity WHERE paper_id = %s AND source = 'rule'",
                (paper_id,),
            )
            cursor.execute(
                "DELETE FROM paper_reference WHERE paper_id = %s", (paper_id,)
            )
            self._save_authors(cursor, paper_id, paper.get("authors", []))
            self._save_keywords(cursor, paper_id, paper.get("keywords", []))
            self._save_entities(cursor, paper_id, paper)
            self._save_references(cursor, paper_id, paper.get("references", []))
            return outcome
        finally:
            cursor.close()

    def _save_authors(
        self, cursor: Any, paper_id: int, authors: list[dict[str, Any]]
    ) -> None:
        for order, author in enumerate(authors, start=1):
            orcid = self._limit(author.get("orcid"), 32)
            if orcid:
                cursor.execute("SELECT id FROM author WHERE orcid = %s", (orcid,))
            else:
                cursor.execute(
                    """
                    SELECT id FROM author
                    WHERE normalized_name = %s AND full_name = %s
                    ORDER BY id LIMIT 1
                    """,
                    (
                        self._limit(author["normalized_name"], 500),
                        self._limit(author["full_name"], 500),
                    ),
                )
            existing = cursor.fetchone()
            if existing:
                author_id = int(existing["id"])
            else:
                cursor.execute(
                    """
                    INSERT INTO author (
                        full_name, last_name, fore_name, initials,
                        collective_name, normalized_name, orcid
                    )
                    VALUES (%s, %s, %s, %s, %s, %s, %s)
                    """,
                    (
                        self._limit(author["full_name"], 500),
                        self._limit(author.get("last_name"), 255),
                        self._limit(author.get("fore_name"), 255),
                        self._limit(author.get("initials"), 50),
                        self._limit(author.get("collective_name"), 500),
                        self._limit(author["normalized_name"], 500),
                        orcid,
                    ),
                )
                author_id = int(cursor.lastrowid)
            cursor.execute(
                """
                INSERT INTO paper_author (
                    paper_id, author_id, author_order, is_corresponding, affiliation
                )
                VALUES (%s, %s, %s, 0, %s)
                """,
                (paper_id, author_id, order, author.get("affiliation")),
            )

    def _save_keywords(
        self, cursor: Any, paper_id: int, keywords: list[dict[str, Any]]
    ) -> None:
        for keyword in keywords:
            cursor.execute(
                """
                INSERT INTO keyword (name, normalized_name, keyword_type)
                VALUES (%s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    id = LAST_INSERT_ID(id),
                    name = VALUES(name)
                """,
                (
                    self._limit(keyword["name"], 255),
                    self._limit(keyword["normalized_name"], 255),
                    keyword["keyword_type"],
                ),
            )
            keyword_id = int(cursor.lastrowid)
            cursor.execute(
                """
                INSERT INTO paper_keyword (
                    paper_id, keyword_id, is_major_topic
                )
                VALUES (%s, %s, %s)
                """,
                (paper_id, keyword_id, bool(keyword.get("major_topic"))),
            )

    def _save_entities(self, cursor: Any, paper_id: int, paper: dict[str, Any]) -> None:
        searchable_text = " ".join(
            [
                paper.get("title") or "",
                paper.get("abstract_text") or "",
                " ".join(keyword.get("name", "") for keyword in paper.get("keywords", [])),
            ]
        ).lower()

        matched: set[tuple[str, str]] = set()
        for entity_type, names in ENTITY_DICTIONARY.items():
            for name in names:
                pattern = r"(?<![A-Za-z0-9])" + re.escape(name.lower()) + r"(?![A-Za-z0-9])"
                if re.search(pattern, searchable_text):
                    matched.add((entity_type, name))

        for keyword in paper.get("keywords", []):
            name = keyword.get("name")
            if name:
                matched.add(("KEYWORD", name))

        for entity_type, name in sorted(matched):
            cursor.execute(
                """
                INSERT INTO biomedical_entity (entity_name, entity_type)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
                """,
                (self._limit(name, 255), entity_type),
            )
            entity_id = int(cursor.lastrowid)
            cursor.execute(
                """
                INSERT INTO paper_entity (paper_id, entity_id, source, confidence)
                VALUES (%s, %s, 'rule', %s)
                ON DUPLICATE KEY UPDATE source = VALUES(source),
                    confidence = VALUES(confidence)
                """,
                (paper_id, entity_id, 0.7),
            )

    def _save_references(
        self, cursor: Any, paper_id: int, references: list[dict[str, Any]]
    ) -> None:
        for reference in references:
            cited_pmid = self._limit(reference.get("cited_pmid"), 32)
            cited_doi = self._limit(reference.get("cited_doi"), 255)
            cited_paper_id = None
            if cited_pmid or cited_doi:
                cursor.execute(
                    """
                    SELECT id
                    FROM paper
                    WHERE (%s IS NOT NULL AND pmid = %s)
                       OR (%s IS NOT NULL AND doi = %s)
                    LIMIT 1
                    """,
                    (cited_pmid, cited_pmid, cited_doi, cited_doi),
                )
                existing = cursor.fetchone()
                if existing:
                    cited_paper_id = int(existing["id"])

            cursor.execute(
                """
                INSERT INTO paper_reference (
                    paper_id, cited_paper_id, cited_pmid, cited_doi, citation_text
                )
                VALUES (%s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    cited_paper_id = VALUES(cited_paper_id),
                    cited_doi = COALESCE(VALUES(cited_doi), cited_doi),
                    citation_text = COALESCE(VALUES(citation_text), citation_text)
                """,
                (
                    paper_id,
                    cited_paper_id,
                    cited_pmid,
                    cited_doi,
                    reference.get("citation_text"),
                ),
            )

    def _paper_values(self, paper: dict[str, Any]) -> tuple[Any, ...]:
        return (
            self._limit(paper["title"], 1000),
            paper.get("abstract_text"),
            self._limit(paper.get("journal"), 500),
            paper.get("publication_year"),
            self._limit(paper.get("doi"), 255),
            self._limit(paper.get("pmid"), 32),
            self._limit(paper.get("pmcid"), 32),
            self._limit(paper.get("pdf_url"), 2048),
            self._limit(paper.get("full_text_url"), 2048),
        )

    def _fail_task(self, task_id: int, message: str) -> None:
        cursor = self.connection.cursor()
        try:
            cursor.execute(
                """
                UPDATE crawl_task
                SET task_status = 'failed',
                    error_message = %s,
                    finished_at = %s
                WHERE id = %s
                """,
                (message[:4000], datetime.now(), task_id),
            )
            self.connection.commit()
        finally:
            cursor.close()

    @staticmethod
    def _limit(value: str | None, maximum: int) -> str | None:
        return value[:maximum] if value else None
