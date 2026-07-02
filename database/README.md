# Database

当前开发数据库为 MySQL 8.4，数据库名为 `biomed_literature`。

## 本机状态

- Windows 服务：`MySQL84`
- 监听地址：`127.0.0.1:3306`
- 启动类型：自动
- MySQL 命令目录已加入用户 `PATH`，新终端生效

## 初始化全部表

在项目根目录执行：

```powershell
mysql -h 127.0.0.1 -u root -p < database/mysql/run_all.sql
```

全部脚本均使用 `CREATE TABLE IF NOT EXISTS`，可按编号重复执行：

1. `001_create_database_and_paper.sql`：数据库和文献表
2. `002_create_author.sql`：作者表
3. `003_create_paper_author.sql`：文献作者关联表
4. `004_create_keyword.sql`：关键词表
5. `005_create_paper_keyword.sql`：文献关键词关联表
6. `006_create_tag.sql`：标签表
7. `007_create_paper_tag.sql`：文献标签关联表
8. `008_create_topic.sql`：专题表
9. `009_create_topic_paper.sql`：专题文献关联表
10. `010_create_crawl_task.sql`：采集任务表

`schema.sql` 是早期 PostgreSQL 设计稿，仅供数据模型参考。