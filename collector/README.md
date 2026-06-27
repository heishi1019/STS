# Collector

采集层采用“统一模型 + 数据源适配器”。首版接入 PubMed ESearch 与 EFetch，后续扩展 Europe PMC、Semantic Scholar、PMC OA 和 PubTator3。

`pubmed_smoke_test.py` 会把 PubMed XML 转换为与数据库字段接近的 JSON。正式采集需增加限速、重试、增量游标和失败任务记录。
