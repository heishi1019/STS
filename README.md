# 生物医学文献知识服务与智能综述平台

项目 1 的首版工程，面向科研选题、文献调研与综述撰写。当前已完成项目范围、目录结构、功能模块图、第一版数据库和 PubMed 采集验证代码。

## 目录

```text
毕业设计/
├── frontend/   # React + TypeScript 前端
├── backend/    # FastAPI 业务接口
├── database/   # PostgreSQL 数据模型
├── collector/  # 文献数据采集器
└── docs/       # 项目说明与设计文档
```

## PubMed 快速测试

采集器只依赖 Python 标准库：

```powershell
python collector/pubmed_smoke_test.py --query "diabetes mellitus[Title/Abstract]" --retmax 3 --output collector/samples/pubmed_sample.json
```

正式采集时请增加 `--email` 和 `--api-key`，并遵守 NCBI 请求频率要求。

## 文档

- [项目简介](docs/project-overview.md)
- [功能模块图](docs/module-architecture.md)
- [数据库设计](docs/data-model.md)
- [PubMed 测试记录](docs/pubmed-api-test.md)
