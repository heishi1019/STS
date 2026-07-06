from __future__ import annotations

import sys
import unittest
from pathlib import Path

COLLECTOR_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(COLLECTOR_DIR))

from pubmed_client import normalize, parse_xml


class PubMedClientTests(unittest.TestCase):
    def test_parse_paper_authors_keywords_and_mesh(self) -> None:
        xml = """
        <PubmedArticleSet>
          <PubmedArticle>
            <MedlineCitation>
              <PMID>123456</PMID>
              <Article>
                <Journal>
                  <JournalIssue>
                    <PubDate><MedlineDate>2024 Jan-Feb</MedlineDate></PubDate>
                  </JournalIssue>
                  <Title>Test Journal</Title>
                </Journal>
                <ArticleTitle>Diabetes study</ArticleTitle>
                <Abstract><AbstractText>Study abstract.</AbstractText></Abstract>
                <AuthorList>
                  <Author>
                    <LastName>Smith</LastName>
                    <ForeName>John</ForeName>
                    <Initials>J</Initials>
                    <Identifier Source="ORCID">https://orcid.org/0000-0000-0000-000X</Identifier>
                    <AffiliationInfo><Affiliation>Test University</Affiliation></AffiliationInfo>
                  </Author>
                </AuthorList>
              </Article>
              <KeywordList>
                <Keyword MajorTopicYN="Y">Diabetes</Keyword>
              </KeywordList>
              <MeshHeadingList>
                <MeshHeading>
                  <DescriptorName MajorTopicYN="N">Humans</DescriptorName>
                </MeshHeading>
              </MeshHeadingList>
            </MedlineCitation>
            <PubmedData>
              <ArticleIdList>
                <ArticleId IdType="doi">10.1000/test</ArticleId>
                <ArticleId IdType="pmc">PMC123456</ArticleId>
              </ArticleIdList>
            </PubmedData>
          </PubmedArticle>
        </PubmedArticleSet>
        """
        papers = parse_xml(xml)
        self.assertEqual(1, len(papers))
        paper = papers[0]
        self.assertEqual("123456", paper["pmid"])
        self.assertEqual(2024, paper["publication_year"])
        self.assertEqual("Smith J", paper["authors"][0]["full_name"])
        self.assertEqual("0000-0000-0000-000X", paper["authors"][0]["orcid"])
        self.assertEqual("author", paper["keywords"][0]["keyword_type"])
        self.assertTrue(paper["keywords"][0]["major_topic"])
        self.assertEqual("mesh", paper["keywords"][1]["keyword_type"])
        self.assertTrue(paper["pdf_url"].endswith("/PMC123456/pdf/"))

    def test_normalize_collapses_unicode_and_whitespace(self) -> None:
        self.assertEqual("diabetes mellitus", normalize("  Diabetes　Mellitus  "))


if __name__ == "__main__":
    unittest.main()
