"""Collect PubMed records and persist them to MySQL."""

from __future__ import annotations

import argparse
import json
import os
import sys

from pubmed_client import PubMedClient
from pubmed_mysql import DatabaseConfig, PubMedMySQLCollector


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Search PubMed and write papers, authors and keywords to MySQL."
    )
    parser.add_argument("--query", required=True, help="PubMed search expression")
    parser.add_argument("--retmax", type=int, default=10, help="1-100 records")
    parser.add_argument("--email", default=os.getenv("NCBI_EMAIL"))
    parser.add_argument("--api-key", default=os.getenv("NCBI_API_KEY"))
    parser.add_argument("--db-host", default=os.getenv("DB_HOST", "127.0.0.1"))
    parser.add_argument(
        "--db-port", type=int, default=int(os.getenv("DB_PORT", "3306"))
    )
    parser.add_argument(
        "--db-name", default=os.getenv("DB_NAME", "biomed_literature")
    )
    parser.add_argument("--db-user", default=os.getenv("DB_USERNAME", "root"))
    args = parser.parse_args()
    if not 1 <= args.retmax <= 100:
        parser.error("--retmax must be between 1 and 100")
    if not args.email:
        parser.error("请通过 --email 或 NCBI_EMAIL 提供 NCBI 联系邮箱")
    return args


def main() -> int:
    args = parse_args()
    collector = None
    try:
        client = PubMedClient(email=args.email, api_key=args.api_key)
        collector = PubMedMySQLCollector(
            DatabaseConfig(
                host=args.db_host,
                port=args.db_port,
                database=args.db_name,
                username=args.db_user,
                password=os.getenv("DB_PASSWORD", ""),
            ),
            client,
        )
        result = collector.collect(args.query, args.retmax)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0 if result["failed"] == 0 else 2
    except Exception as error:
        print(f"PubMed 采集失败：{error}", file=sys.stderr)
        return 1
    finally:
        if collector is not None:
            collector.close()


if __name__ == "__main__":
    raise SystemExit(main())
