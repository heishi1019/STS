"""PubMed E-utilities client and XML parser."""

from __future__ import annotations

import json
import re
import time
import unicodedata
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen
from xml.etree import ElementTree as ET

BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils"
TOOL_NAME = "biomedical-literature-platform"
YEAR_PATTERN = re.compile(r"\b(1[5-9]\d{2}|20\d{2}|2100)\b")


class PubMedError(RuntimeError):
    """Raised when PubMed cannot be queried or parsed."""


@dataclass(frozen=True)
class PubMedSearchResult:
    query: str
    total_results: int
    fetched_at: str
    papers: list[dict[str, Any]]


def normalize(value: str) -> str:
    return " ".join(unicodedata.normalize("NFKC", value).split()).lower()


def text(node: ET.Element | None) -> str | None:
    if node is None:
        return None
    value = " ".join("".join(node.itertext()).split())
    return value or None


def article_id(article: ET.Element, kind: str) -> str | None:
    for node in article.findall(".//PubmedData/ArticleIdList/ArticleId"):
        if node.attrib.get("IdType", "").lower() == kind.lower():
            return text(node)
    return None


def publication_year(article: ET.Element) -> int | None:
    candidates = [
        text(article.find(".//Article/Journal/JournalIssue/PubDate/Year")),
        text(article.find(".//Article/Journal/JournalIssue/PubDate/MedlineDate")),
        text(article.find(".//Article/ArticleDate/Year")),
        text(article.find(".//MedlineCitation/DateCompleted/Year")),
    ]
    for candidate in candidates:
        if candidate and (match := YEAR_PATTERN.search(candidate)):
            return int(match.group(1))
    return None


def clean_orcid(value: str | None) -> str | None:
    if not value:
        return None
    return re.sub(r"^https?://orcid\.org/", "", value, flags=re.IGNORECASE).strip() or None


def parse_authors(article: ET.Element) -> list[dict[str, Any]]:
    result: list[dict[str, Any]] = []
    for node in article.findall(".//Article/AuthorList/Author"):
        last_name = text(node.find("./LastName"))
        fore_name = text(node.find("./ForeName"))
        initials = text(node.find("./Initials"))
        collective_name = text(node.find("./CollectiveName"))
        if collective_name:
            full_name = collective_name
        elif last_name and initials:
            full_name = f"{last_name} {initials}"
        elif last_name and fore_name:
            full_name = f"{last_name} {fore_name}"
        else:
            full_name = last_name or fore_name
        if not full_name:
            continue

        orcid = None
        for identifier in node.findall("./Identifier"):
            if identifier.attrib.get("Source", "").upper() == "ORCID":
                orcid = clean_orcid(text(identifier))
                break
        affiliations = [
            value
            for item in node.findall("./AffiliationInfo/Affiliation")
            if (value := text(item))
        ]
        result.append(
            {
                "full_name": full_name,
                "last_name": last_name,
                "fore_name": fore_name,
                "initials": initials,
                "collective_name": collective_name,
                "normalized_name": normalize(full_name),
                "orcid": orcid,
                "affiliation": "; ".join(affiliations) or None,
            }
        )
    return result


def parse_keywords(article: ET.Element) -> list[dict[str, Any]]:
    result: dict[tuple[str, str], dict[str, Any]] = {}
    for node in article.findall(".//MedlineCitation/KeywordList/Keyword"):
        name = text(node)
        if name:
            key = ("author", normalize(name))
            result.setdefault(
                key,
                {
                    "name": name,
                    "normalized_name": key[1],
                    "keyword_type": "author",
                    "major_topic": node.attrib.get("MajorTopicYN") == "Y",
                },
            )
    for node in article.findall(
        ".//MedlineCitation/MeshHeadingList/MeshHeading/DescriptorName"
    ):
        name = text(node)
        if name:
            key = ("mesh", normalize(name))
            result.setdefault(
                key,
                {
                    "name": name,
                    "normalized_name": key[1],
                    "keyword_type": "mesh",
                    "major_topic": node.attrib.get("MajorTopicYN") == "Y",
                },
            )
    return list(result.values())


def parse_xml(payload: bytes | str) -> list[dict[str, Any]]:
    try:
        root = ET.fromstring(payload)
    except ET.ParseError as error:
        raise PubMedError("PubMed XML 解析失败") from error

    papers: list[dict[str, Any]] = []
    for item in root.findall("./PubmedArticle"):
        abstract_parts = [
            value
            for node in item.findall(".//Article/Abstract/AbstractText")
            if (value := text(node))
        ]
        pmcid = article_id(item, "pmc")
        papers.append(
            {
                "pmid": text(item.find("./MedlineCitation/PMID")),
                "pmcid": pmcid,
                "doi": article_id(item, "doi"),
                "title": text(item.find(".//Article/ArticleTitle")),
                "abstract_text": "\n".join(abstract_parts) or None,
                "journal": text(item.find(".//Article/Journal/Title")),
                "publication_year": publication_year(item),
                "data_source": "pubmed",
                "pdf_url": (
                    f"https://pmc.ncbi.nlm.nih.gov/articles/{pmcid}/pdf/"
                    if pmcid
                    else None
                ),
                "full_text_url": (
                    f"https://pmc.ncbi.nlm.nih.gov/articles/{pmcid}/"
                    if pmcid
                    else None
                ),
                "authors": parse_authors(item),
                "keywords": parse_keywords(item),
            }
        )
    return papers


class PubMedClient:
    def __init__(
        self,
        email: str,
        api_key: str | None = None,
        timeout: int = 30,
        max_retries: int = 3,
    ) -> None:
        if not email or "@" not in email:
            raise ValueError("NCBI_EMAIL 必须是有效邮箱")
        self.email = email
        self.api_key = api_key
        self.timeout = timeout
        self.max_retries = max_retries
        self._last_request_at = 0.0
        self._minimum_interval = 0.11 if api_key else 0.35

    def fetch(self, query: str, retmax: int) -> PubMedSearchResult:
        search = json.loads(
            self._request(
                "esearch.fcgi",
                {
                    "db": "pubmed",
                    "term": query,
                    "retmax": retmax,
                    "retmode": "json",
                    "sort": "pub date",
                },
            ).decode("utf-8")
        )["esearchresult"]
        ids = search.get("idlist", [])
        papers = (
            parse_xml(
                self._request(
                    "efetch.fcgi",
                    {
                        "db": "pubmed",
                        "id": ",".join(ids),
                        "retmode": "xml",
                    },
                )
            )
            if ids
            else []
        )
        return PubMedSearchResult(
            query=query,
            total_results=int(search.get("count", 0)),
            fetched_at=datetime.now(timezone.utc).isoformat(),
            papers=papers,
        )

    def _request(self, endpoint: str, params: dict[str, str | int]) -> bytes:
        common: dict[str, str] = {"tool": TOOL_NAME, "email": self.email}
        if self.api_key:
            common["api_key"] = self.api_key
        url = f"{BASE_URL}/{endpoint}?{urlencode({**common, **params})}"

        last_error: Exception | None = None
        for attempt in range(self.max_retries):
            self._wait_for_rate_limit()
            request = Request(
                url,
                headers={
                    "User-Agent": f"{TOOL_NAME}/1.0 ({self.email})",
                    "Accept": "application/json, application/xml, text/xml",
                },
            )
            try:
                with urlopen(request, timeout=self.timeout) as response:
                    return response.read()
            except HTTPError as error:
                if error.code not in {429, 500, 502, 503, 504}:
                    raise PubMedError(f"PubMed HTTP 错误：{error.code}") from error
                last_error = error
            except (URLError, TimeoutError) as error:
                last_error = error
            if attempt + 1 < self.max_retries:
                time.sleep(2**attempt)

        raise PubMedError(f"PubMed 请求失败：{last_error}") from last_error

    def _wait_for_rate_limit(self) -> None:
        elapsed = time.monotonic() - self._last_request_at
        if elapsed < self._minimum_interval:
            time.sleep(self._minimum_interval - elapsed)
        self._last_request_at = time.monotonic()
