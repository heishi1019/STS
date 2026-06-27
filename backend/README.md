# Backend

计划采用 FastAPI，首版接口边界包括 `/papers`、`/topics`、`/tags`、`/statistics`、`/collections`；`/ai` 作为毕业设计阶段的扩展接口。采集任务与用户请求分开运行，避免外部数据源限流影响检索服务。
