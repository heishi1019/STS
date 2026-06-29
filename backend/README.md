# Backend

生物医学文献知识服务与智能综述平台后端，采用 Spring Boot 3.5.15、JDK 17、Maven、Spring Web、MySQL、Lombok 和 MyBatis-Plus 3.5.15。

## 环境要求

- JDK 17+
- MySQL 8+
- 无需预装 Maven，仓库已包含 Maven Wrapper

## 创建数据库

```sql
CREATE DATABASE biomedical_literature
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

数据库连接通过环境变量覆盖：

- `DB_URL`：默认 `jdbc:mysql://localhost:3306/biomedical_literature...`
- `DB_USERNAME`：默认 `root`
- `DB_PASSWORD`：默认空
- `SERVER_PORT`：默认 `8080`

## 运行

```powershell
cd E:\毕业设计\backend
.\mvnw.cmd spring-boot:run
```

健康检查：`GET http://localhost:8080/api/health`

## 测试与打包

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

> 根目录现有 `database/schema.sql` 使用 PostgreSQL 语法，正式采用 MySQL 前需另行转换，不能直接执行。
