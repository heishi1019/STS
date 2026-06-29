# Database

当前开发数据库为 MySQL 8.4，数据库名为 `biomed_literature`。

## 本机状态

- Windows 服务：`MySQL84`
- 监听地址：`127.0.0.1:3306`
- 启动类型：自动
- MySQL 命令目录已加入用户 `PATH`，新终端生效

## 初始化或重复检查

在项目根目录执行：

```powershell
mysql -h 127.0.0.1 -u root -p < database/mysql/001_create_database_and_paper.sql
```

脚本可重复执行，会创建 `biomed_literature` 数据库和首张 `paper` 表。

- `mysql/001_create_database_and_paper.sql`：当前 MySQL 建库建表脚本
- `schema.sql`：早期 PostgreSQL 设计稿，仅供数据模型参考