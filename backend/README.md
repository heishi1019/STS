# Backend

生物医学文献知识服务与智能综述平台后端，采用 Spring Boot 3.5.15、JDK 17、Maven、Spring Web、MySQL、Lombok 和 MyBatis-Plus 3.5.15。

## 环境要求

- JDK 17+
- MySQL 8.4+（本机服务名：`MySQL84`）
- 无需预装 Maven，仓库已包含 Maven Wrapper

## 数据库

数据库名为 `biomed_literature`，初始化脚本位于：

```text
database/schema.sql
```

`database/schema.sql` 是正式的 MySQL 全量初始化文件；`database/mysql/` 保存按步骤执行的增量脚本。

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

健康检查：

- 应用：`GET http://localhost:8080/api/health`
- 数据库：`GET http://localhost:8080/api/health/database`

## Paper API

- `GET /api/papers`：分页列表，可选 `keyword`、`year`、`dataSource`
- `GET /api/papers/{id}`：文献聚合详情，包含作者、关键词和标签
- `POST /api/papers`：新增文献
- `PUT /api/papers/{id}`：修改文献
- `DELETE /api/papers/{id}`：删除文献

文献详情返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "文献标题",
    "abstractText": "摘要",
    "journal": "期刊",
    "publishYear": 2024,
    "doi": "10.xxxx",
    "pmid": "123456",
    "authors": [
      {
        "id": 1,
        "name": "Smith J"
      }
    ],
    "keywords": [
      {
        "id": 1,
        "keyword": "diabetes"
      }
    ],
    "tags": [
      {
        "id": 1,
        "tagName": "重点文献",
        "color": "#409EFF"
      }
    ]
  }
}
```

## 标签与专题 API

- `GET /api/tags`：标签列表
- `POST /api/tags`：新增标签
- `PUT /api/tags/{id}`：修改标签
- `DELETE /api/tags/{id}`：删除标签
- `GET /api/topics`：专题列表
- `POST /api/topics`：新增专题
- `PUT /api/topics/{id}`：修改专题
- `DELETE /api/topics/{id}`：删除专题

## 文献关联 API

- `POST /api/papers/{paperId}/tags/{tagId}`：为文献添加标签
- `DELETE /api/papers/{paperId}/tags/{tagId}`：移除文献标签
- `GET /api/papers/{paperId}/tags`：查询文献标签
- `POST /api/topics/{topicId}/papers/{paperId}`：将文献加入专题
- `DELETE /api/topics/{topicId}/papers/{paperId}`：从专题移除文献
- `GET /api/topics/{topicId}/papers`：查询专题文献

所有接口统一返回 `code`、`message`、`data`。

## 测试与打包

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```
