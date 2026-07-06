# Collector

采集层采用“统一模型 + 数据源适配器”。首版已接入 PubMed ESearch 与 EFetch，可将文献、作者、作者关键词、MeSH 主题词及关联关系写入 MySQL，后续扩展 Europe PMC、Semantic Scholar、PMC OA 和 PubTator3。

官方参考：

- [NCBI E-utilities 使用规范](https://www.ncbi.nlm.nih.gov/books/NBK25497/)
- [MySQL Connector/Python 安装说明](https://dev.mysql.com/doc/connector-python/en/quick-installation-guide.html)

## 安装依赖

```powershell
cd E:\毕业设计
py -3.11 -m pip install -r collector\requirements.txt
```

## 环境变量

```powershell
$env:NCBI_EMAIL = "<你的联系邮箱>"
$env:NCBI_API_KEY = "<可选的 NCBI API Key>"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<本机 MySQL 密码>"
```

数据库地址默认是 `127.0.0.1:3306/biomed_literature`，可通过 `DB_HOST`、`DB_PORT`、`DB_NAME` 修改。

`NCBI_EMAIL` 用于遵守 NCBI 的工具联系信息要求。没有 API Key 也能采集；配置 Key 后可使用 NCBI 提供的更高请求额度。

## 采集并写入 MySQL

```powershell
py -3.11 collector\pubmed_to_mysql.py `
  --query "diabetes mellitus[Title/Abstract]" `
  --retmax 10
```

程序会：

- 使用 ESearch 获取 PMID，再通过 EFetch 批量获取 XML；
- 无 API Key 时将请求速率限制在每秒 3 次以内；
- 遇到临时网络错误或 429/5xx 时自动重试；
- 按 PMID 优先、DOI 其次识别已有文献，重复采集时更新而非重复插入；
- 写入 `paper`、`author`、`paper_author`、`keyword`、`paper_keyword`；
- 在 `crawl_task` 记录总数、处理数、成功数、失败数和错误信息。

## 仅测试 PubMed

`pubmed_smoke_test.py` 只生成 JSON，不写数据库：

```powershell
py -3.11 collector\pubmed_smoke_test.py `
  --query "diabetes mellitus[Title/Abstract]" `
  --retmax 3
```

## 自动测试

```powershell
py -3.11 -m unittest discover -s collector\tests -v
```
