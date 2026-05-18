# ADR-001: 独立微服务部署与 MyBatis-Plus + MySQL 8

## 状态

已接受（Accepted）

## 日期

2026-05-17

## 背景

UM-Core 定位为**通用用户管理系统**，需可被多个宿主业务系统复用。需在持久层、部署形态上做出可长期维护的架构决策。

## 决策

1. **部署形态**：采用**独立微服务**部署，宿主系统通过 **HTTPS + RESTful API** 集成，不共享数据库连接池。
2. **持久层**：**MyBatis-Plus 3.5.x** + **MySQL 8**。
3. **数据库迁移**：**Flyway**，脚本位于 `um-bootstrap/src/main/resources/db/migration`。
4. **缓存**：**Redis** 用于 Token、验证码、登录限流、Refresh Token 存储。
5. **API 文档**：**springdoc-openapi 3**。

## 理由

### 独立微服务

- 数据主权清晰：UM 库独立，用户 ID 作为全局标识供宿主关联。
- 版本与发布解耦：宿主无需升级 Spring Boot 版本即可消费 UM API。
- 安全边界明确：认证、风控、VIP 状态集中在 UM 域内演进。

### MyBatis-Plus + MySQL

- 复杂 SQL（VIP 升级折算、公告受众筛选）便于在 XML 中显式维护。
- 团队熟悉度高，与 Spring Boot 3 集成成熟。
- MySQL 8 JSON 类型支持用户资料扩展字段 `extension_json`。

## 后果

### 正面

- 宿主集成简单：HTTP 客户端 + API Key / OAuth2 Client。
- SQL 与领域逻辑可追溯，便于 DBA 审查索引。

### 负面

- 网络延迟与分布式一致性需宿主侧处理（重试、幂等键）。
- 需独立运维 MySQL、Redis 与 UM 服务实例。

## 未采纳方案

| 方案 | 未采纳原因 |
|------|------------|
| Library/Starter 同进程嵌入 | 增加宿主 classpath 冲突与版本耦合；当前以微服务为先 |
| Spring Data JPA | 复杂查询与 SQL 调优成本较高；已选 MyBatis-Plus |
| PostgreSQL | 团队与运维默认 MySQL；后续可通过 ADR 增补多库支持 |

## 关联文档

- [ARCHITECTURE.md](../ARCHITECTURE.md)
- [EMBEDDING_AND_INTEGRATION.md](../EMBEDDING_AND_INTEGRATION.md)
- [conventions/06-database-and-mybatis.md](../conventions/06-database-and-mybatis.md)
