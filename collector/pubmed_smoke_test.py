"""Fetch a small PubMed sample without writing to MySQL."""

from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

from pubmed_client import PubMedClient, PubMedError


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--query", default="diabetes mellitus[Title/Abstract]")
    parser.add_argument("--retmax", type=int, default=3)
    parser.add_argument("--email", default=os.getenv("NCBI_EMAIL"))
    parser.add_argument("--api-key", default=os.getenv("NCBI_API_KEY"))
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("collector/samples/pubmed_sample.json"),
    )
    args = parser.parse_args()
    if not 1 <= args.retmax <= 100:
        parser.error("--retmax must be between 1 and 100")
    if not args.email:
        parser.error("请通过 --email 或 NCBI_EMAIL 提供 NCBI 联系邮箱")
    try:
        fetched = PubMedClient(args.email, args.api_key).fetch(
            args.query, args.retmax
        )
        result = {
            "query": fetched.query,
            "total_results": fetched.total_results,
            "returned_results": len(fetched.papers),
            "fetched_at": fetched.fetched_at,
            "papers": fetched.papers,
        }
    except (PubMedError, ValueError) as error:
        print(f"PubMed smoke test failed: {error}", file=sys.stderr)
        return 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(
        "PubMed smoke test passed: "
        f"fetched {result['returned_results']} "
        f"of {result['total_results']} records"
    )
    print(f"Saved to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
