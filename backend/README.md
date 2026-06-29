# Backend

生物医学文献知识服务与智能综述平台后端，采用 Spring Boot 3.5.15、JDK 17、Maven、Spring Web、MySQL、Lombok 和 MyBatis-Plus 3.5.15。

## 环境要求

- JDK 17+
- MySQL 8.4+（本机服务名：`MySQL84`）
- 无需预装 Maven，仓库已包含 Maven Wrapper

## 数据库

数据库名为 `biomed_literature`，初始化脚本位于：

```text
database/mysql/001_create_database_and_paper.sql
```

数据库连接可通过环境变量覆盖：

- `DB_URL`：默认连接本机 `biomed_literature`
- `DB_USERNAME`：默认 `root`
- `DB_PASSWORD`：必须在本地环境设置，不写入 Git
- `SERVER_PORT`：默认 `8080`

## 运行

```powershell
cd E:\毕业设计\backend
$env:DB_PASSWORD = "<本机 MySQL 密码>"
.\mvnw.cmd spring-boot:run
```

健康检查：`GET http://localhost:8080/api/health`

## 测试与打包

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

> 根目录 `database/schema.sql` 是早期 PostgreSQL 设计稿，不能在 MySQL 中直接执行。