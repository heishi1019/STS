"""PubMed E-utilities smoke test using only the Python standard library."""

from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen
from xml.etree import ElementTree as ET

BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils"
TOOL_NAME = "biomedical-literature-platform"


def request(endpoint: str, params: dict[str, str | int]) -> bytes:
    url = f"{BASE_URL}/{endpoint}?{urlencode(params)}"
    req = Request(url, headers={"User-Agent": f"{TOOL_NAME}/0.1"})
    with urlopen(req, timeout=30) as response:
        return response.read()


def text(node: ET.Element | None) -> str | None:
    if node is None:
        return None
    value = " ".join("".join(node.itertext()).split())
    return value or None


def article_id(article: ET.Element, kind: str) -> str | None:
    for node in article.findall(".//PubmedData/ArticleIdList/ArticleId"):
        if node.attrib.get("IdType") == kind:
            return text(node)
    return None


def parse_authors(article: ET.Element) -> list[dict[str, Any]]:
    result = []
    for node in article.findall(".//Article/AuthorList/Author"):
        result.append({
            "last_name": text(node.find("./LastName")),
            "fore_name": text(node.find("./ForeName")),
            "initials": text(node.find("./Initials")),
            "collective_name": text(node.find("./CollectiveName")),
            "affiliations": [text(x) for x in node.findall("./AffiliationInfo/Affiliation") if text(x)],
        })
    return result


def parse_xml(payload: bytes) -> list[dict[str, Any]]:
    root = ET.fromstring(payload)
    papers = []
    for item in root.findall("./PubmedArticle"):
        journal = item.find(".//Article/Journal")
        pub_date = item.find(".//JournalIssue/PubDate")
        papers.append({
            "pmid": text(item.find("./MedlineCitation/PMID")),
            "pmcid": article_id(item, "pmc"),
            "doi": article_id(item, "doi"),
            "title": text(item.find(".//Article/ArticleTitle")),
            "abstract": "\n".join(filter(None, [text(x) for x in item.findall(".//Abstract/AbstractText")])) or None,
            "journal": {
                "title": text(journal.find("./Title")) if journal is not None else None,
                "iso_abbreviation": text(journal.find("./ISOAbbreviation")) if journal is not None else None,
                "issn": text(journal.find("./ISSN")) if journal is not None else None,
            },
            "publication_date": {
                "year": text(pub_date.find("./Year")) if pub_date is not None else None,
                "month": text(pub_date.find("./Month")) if pub_date is not None else None,
                "day": text(pub_date.find("./Day")) if pub_date is not None else None,
                "medline_date": text(pub_date.find("./MedlineDate")) if pub_date is not None else None,
            },
            "authors": parse_authors(item),
            "keywords": [text(x) for x in item.findall(".//KeywordList/Keyword") if text(x)],
            "mesh_terms": [text(x) for x in item.findall(".//MeshHeading/DescriptorName") if text(x)],
            "language": text(item.find(".//Article/Language")),
            "source": "pubmed",
        })
    return papers


def fetch(query: str, retmax: int, email: str | None, api_key: str | None) -> dict[str, Any]:
    common: dict[str, str | int] = {"tool": TOOL_NAME}
    if email:
        common["email"] = email
    if api_key:
        common["api_key"] = api_key
    search = json.loads(request("esearch.fcgi", {
        **common, "db": "pubmed", "term": query, "retmax": retmax,
        "retmode": "json", "sort": "pub date",
    }).decode("utf-8"))["esearchresult"]
    ids = search.get("idlist", [])
    papers = parse_xml(request("efetch.fcgi", {
        **common, "db": "pubmed", "id": ",".join(ids), "retmode": "xml",
    })) if ids else []
    return {
        "query": query,
        "total_results": int(search.get("count", 0)),
        "returned_results": len(papers),
        "fetched_at": datetime.now(timezone.utc).isoformat(),
        "papers": papers,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--query", default="diabetes mellitus[Title/Abstract]")
    parser.add_argument("--retmax", type=int, default=3)
    parser.add_argument("--email")
    parser.add_argument("--api-key")
    parser.add_argument("--output", type=Path, default=Path("collector/samples/pubmed_sample.json"))
    args = parser.parse_args()
    if not 1 <= args.retmax <= 100:
        parser.error("--retmax must be between 1 and 100")
    try:
        result = fetch(args.query, args.retmax, args.email, args.api_key)
    except (HTTPError, URLError, TimeoutError, ET.ParseError, ValueError) as error:
        print(f"PubMed smoke test failed: {error}", file=sys.stderr)
        return 1
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"PubMed smoke test passed: fetched {result['returned_results']} of {result['total_results']} records")
    print(f"Saved to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
