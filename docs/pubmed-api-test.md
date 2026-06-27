# PubMed API 测试记录

## 测试目标

验证 NCBI E-utilities 的最小链路：ESearch 检索 PMID，EFetch 获取文献 XML，采集器转换统一 JSON。

- 检索式：`diabetes mellitus[Title/Abstract]`
- 返回数量：3
- 输出文件：`collector/samples/pubmed_sample.json`

```powershell
python collector/pubmed_smoke_test.py --query "diabetes mellitus[Title/Abstract]" --retmax 3 --output collector/samples/pubmed_sample.json
```

采集字段包括 PMID、PMCID、DOI、标题、摘要、期刊、出版日期、作者、关键词、MeSH 和语种。

## 实际结果

- 测试日期：2026-06-27（Asia/Shanghai）
- ESearch 总命中数：313,902
- 成功获取并解析：3 篇
- PMID：`42322207`、`42101192`、`42096241`
- 标准 JSON：`collector/samples/pubmed_sample.json`
- 原始 EFetch XML：`collector/samples/pubmed_efetch_sample.xml`

结论：PubMed 的 ESearch → EFetch 数据链路连通，PMID、DOI、标题、摘要、期刊、作者、关键词与 MeSH 等字段可供后续入库。正式采集应提供联系邮箱和 API Key，并实现限速、重试和断点续采；不能假设每篇文献都有 DOI、摘要、关键词或开放 PDF。
