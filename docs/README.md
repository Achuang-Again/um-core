# UM-Core 文档中心

通用用户管理微服务（UM-Core）的架构说明与 **AI 编程规约**。人类开发者与 AI Agent 在修改本仓库前，应优先阅读本文档导航。

## 阅读顺序（推荐）

| 顺序 | 文档 | 受众 | 说明 |
|------|------|------|------|
| 1 | [AI_PROGRAMMING_CONVENTIONS.md](./AI_PROGRAMMING_CONVENTIONS.md) | **AI / 开发者** | 主规约入口，含 21 维度索引与 PR 自检清单 |
| 2 | [ARCHITECTURE.md](./ARCHITECTURE.md) | 架构师 / 开发者 | 总体架构、技术栈、部署视图 |
| 3 | [MODULE_STRUCTURE.md](./MODULE_STRUCTURE.md) | 开发者 | Maven 多模块职责与依赖规则 |
| 4 | [EMBEDDING_AND_INTEGRATION.md](./EMBEDDING_AND_INTEGRATION.md) | 宿主系统对接 | HTTP 集成、租户、认证 |
| 5 | [conventions/](./conventions/) | AI / 开发者 | 分卷细则（实现时按需查阅） |
| 6 | [adr/](./adr/) | 架构师 | 架构决策记录 |

## 分卷规约索引

| 编号 | 文件 | 主题 |
|------|------|------|
| 01 | [01-project-and-maven.md](./conventions/01-project-and-maven.md) | Maven 多模块与项目边界 |
| 02 | [02-java-and-spring.md](./conventions/02-java-and-spring.md) | Java 21 与 Spring Boot 3 |
| 03 | [03-package-and-naming.md](./conventions/03-package-and-naming.md) | 包结构与命名约定 |
| 04 | [04-rest-api.md](./conventions/04-rest-api.md) | RESTful API 契约 |
| 05 | [05-security-and-auth.md](./conventions/05-security-and-auth.md) | 安全与认证鉴权 |
| 06 | [06-database-and-mybatis.md](./conventions/06-database-and-mybatis.md) | 数据库与 MyBatis-Plus |
| 07 | [07-domain-auth-profile-vip.md](./conventions/07-domain-auth-profile-vip.md) | 认证 / 资料 / VIP 领域规则 |
| 08 | [08-announcement.md](./conventions/08-announcement.md) | 公告模块 |
| 09 | [09-error-logging-observability.md](./conventions/09-error-logging-observability.md) | 异常、日志与可观测性 |
| 10 | [10-testing.md](./conventions/10-testing.md) | 测试规范 |
| 11 | [11-configuration-secrets.md](./conventions/11-configuration-secrets.md) | 配置与密钥管理 |
| 12 | [12-git-and-ai-workflow.md](./conventions/12-git-and-ai-workflow.md) | Git 与 AI 工作流 |

## 架构决策记录（ADR）

| ID | 标题 |
|----|------|
| [ADR-001](./adr/ADR-001-standalone-microservice.md) | 独立微服务 + MyBatis-Plus + MySQL 8 |

## 仓库工作边界

| 级别 | 规则 |
|------|------|
| ✅ 总是 | 提交前运行 `mvn test`；遵循命名与 API 契约；变更接口时同步 OpenAPI |
| ⚠️ 先询问 | 数据库模式变更（Flyway）；父 POM 新增依赖 |
| 🚫 绝不 | 提交密钥；编辑 `node_modules/`；修改 CI 配置（`.github/workflows` 等） |

## 与 Cursor / Agent 衔接

建议在项目 `.cursor/rules/` 中增加短规则：

> 实现或修改 UM-Core 代码前，必须先阅读 `docs/AI_PROGRAMMING_CONVENTIONS.md`，并遵守 `docs/conventions/` 分卷细则。

## 技术栈速查

- Java 21、Maven 3.9.16、Spring Boot 3.2.x
- MyBatis-Plus 3.5.x、MySQL 8、Redis、Flyway
- 对外：RESTful API（独立微服务部署）
