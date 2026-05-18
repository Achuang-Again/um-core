# UM-Core AI 编程规约（主入口）

> **面向 AI Agent 与人类开发者**：在修改本仓库任何代码、SQL、配置前，必须阅读本文档及所引用的分卷细则。分卷位于 [conventions/](./conventions/)。

## 0. 元信息

| 项 | 值 |
|----|-----|
| 项目 | UM-Core 通用用户管理微服务 |
| 技术栈 | Java 21、Maven 3.9.16、Spring Boot 3.2.x、MyBatis-Plus、MySQL 8、Redis |
| 对外协议 | RESTful API |
| 部署 | 独立微服务（宿主 HTTP 调用） |

---

## 1. 角色与目标

### 1.1 AI 在本仓库的职责

- 实现或修改 **UM 域内** 功能：认证、资料、VIP、公告。
- 遵循多模块边界与分层架构。
- 编写/更新测试、Flyway 脚本（**schema 变更须先询问**）、OpenAPI 注解。

### 1.2 AI 不得做的事

- 修改 CI 配置（`.github/workflows`、Jenkinsfile 等）。
- 提交密钥、真实密码、生产连接串。
- 编辑 `node_modules/`（本仓库非 Node 主项目）。
- 在业务模块新增 `@RestController`（仅 `um-api` 允许）。
- 为 VIP 过期实现 Cron 全表扫描降级。

---

## 2. 仓库工作边界

| 级别 | 规则 |
|------|------|
| ✅ **总是** | 提交前执行 `mvn clean test`；遵循命名与包结构；API 变更同步 OpenAPI；新增类放在正确模块与 layer |
| ⚠️ **先询问** | Flyway 迁移 / 表结构变更；父 POM 新增第三方依赖；错误码段新增 |
| 🚫 **绝不** | 提交密钥；改 CI；物理删除 `um_user`；日志打印 password/token/验证码 |

---

## 3. 21 维度索引（分卷映射）

| # | 维度 | 分卷 |
|---|------|------|
| 1 | Maven 多模块与项目边界 | [01-project-and-maven.md](./conventions/01-project-and-maven.md) |
| 2 | Java 21 语言特性 | [02-java-and-spring.md](./conventions/02-java-and-spring.md) |
| 3 | 包结构与命名 | [03-package-and-naming.md](./conventions/03-package-and-naming.md) |
| 4 | RESTful API | [04-rest-api.md](./conventions/04-rest-api.md) |
| 5 | 安全与认证 | [05-security-and-auth.md](./conventions/05-security-and-auth.md) |
| 6 | 数据库与 MyBatis-Plus | [06-database-and-mybatis.md](./conventions/06-database-and-mybatis.md) |
| 7 | 领域：认证 / 资料 / VIP | [07-domain-auth-profile-vip.md](./conventions/07-domain-auth-profile-vip.md) |
| 8 | 公告模块 | [08-announcement.md](./conventions/08-announcement.md) |
| 9 | 异常、日志、可观测性 | [09-error-logging-observability.md](./conventions/09-error-logging-observability.md) |
| 10 | 测试 | [10-testing.md](./conventions/10-testing.md) |
| 11 | 配置与密钥 | [11-configuration-secrets.md](./conventions/11-configuration-secrets.md) |
| 12 | Git 与 AI 工作流 | [12-git-and-ai-workflow.md](./conventions/12-git-and-ai-workflow.md) |

架构背景：[ARCHITECTURE.md](./ARCHITECTURE.md)、[MODULE_STRUCTURE.md](./MODULE_STRUCTURE.md)、[EMBEDDING_AND_INTEGRATION.md](./EMBEDDING_AND_INTEGRATION.md)。

---

## 4. 核心原则（摘要）

### 4.1 分层

```
Controller (um-api) → ApplicationService (um-*) → Domain → Repository (um-infrastructure)
```

- Controller **只做**：参数校验、调用 Application、封装 `ApiResponse`。
- **禁止** Controller 直接调用 Mapper。

### 4.2 统一响应

所有 REST 返回 `ApiResponse<T>`，`code=0` 表示成功。详见分卷 04。

### 4.3 安全

- 密码：BCrypt，强度 factor ≥ 10。
- 认证：JWT Access（≤2h）+ Refresh（≤30d），Refresh 存 Redis。
- 注销：软删除 + 匿名化，见分卷 07。

### 4.4 VIP 惰性过期

- 读路径：`expire_at < now()` → 对调用方返回非 VIP。
- 写路径：异步 `markExpired`，**禁止**定时任务扫表降级。

### 4.5 数据表

- 前缀 `um_`；审计字段齐全；用户主表禁止物理 DELETE。

---

## 5. 快速决策表

| 场景 | 做法 |
|------|------|
| 新增 HTTP 接口 | `um-api` 新增 Controller + DTO；调用对应 `um-*` ApplicationService |
| 新增表 | 先询问；`um-bootstrap` 新增 `V{n}__*.sql`；PO/Mapper 在 infrastructure |
| 跨模块调用 | 通过 `um-api` 编排，或提取到 `um-domain` 接口由 infrastructure 实现 |
| 扩展用户字段 | 写入 `um_user_profile.extension_json`，不随意加列 |
| 缓存 | Redis，键前缀 `um:`，见分卷 11 |

---

## 6. AI 执行检查清单（PR / 提交前）

复制以下清单，逐项自检：

### 6.1 边界与流程

- [ ] 已阅读相关分卷规约
- [ ] 未修改 CI 配置
- [ ] 未提交任何密钥或真实凭证
- [ ] 若变更 DB，已获维护者确认
- [ ] 若新增依赖，已获维护者确认

### 6.2 结构与命名

- [ ] 类位于正确 Maven 模块与 `com.um.core.*` 包
- [ ] 仅 `um-api` 含 Controller
- [ ] `um-domain` 无 Spring/MyBatis 依赖
- [ ] 命名符合分卷 03（类、方法、表、API 路径）

### 6.3 API

- [ ] 路径以 `/api/v1/um/` 开头
- [ ] 返回 `ApiResponse`，错误码在正确段位
- [ ] DTO 使用 Record + Validation 注解
- [ ] OpenAPI `@Operation` 已更新

### 6.4 安全

- [ ] 密码 BCrypt 存储，日志无明文密码
- [ ] Token/验证码未写入日志
- [ ] 敏感接口需认证
- [ ] 注销为软删除 + 匿名化

### 6.5 数据访问

- [ ] PO 与 Domain 分离，Converter 明确
- [ ] 无生产代码 `SELECT *`
- [ ] 逻辑删除使用 `@TableLogic` 或 `deleted_at` 一致
- [ ] 索引与 Flyway 脚本同步

### 6.6 领域逻辑

- [ ] VIP 查询含惰性过期判断
- [ ] 未新增 Cron 扫表降级 VIP
- [ ] VIP 写操作支持幂等键（如适用）
- [ ] 资料更新检查 `version` 乐观锁

### 6.7 质量

- [ ] 单元测试覆盖新增 Application/Domain 逻辑
- [ ] `mvn clean test` 通过
- [ ] 无 `System.out.println` 调试残留
- [ ] traceId 在日志中可关联

### 6.8 配置

- [ ] 敏感项使用环境变量 `UM_*`
- [ ] 示例配置无真实密钥

---

## 7. 错误码段位（速查）

| 段位 | 范围示例 | 域 |
|------|----------|-----|
| 通用 | 10001–10999 | 参数、系统 |
| 认证 | 20001–20999 | 登录、注册、Token |
| 资料 | 30001–30999 | Profile |
| VIP | 40001–40999 | 订阅、升级 |
| 公告 | 50001–50999 | Announcement |

新增错误码须在 `um-common` 的 `ErrorCodes` 中集中定义。

---

## 8. 代码生成模板（AI 引用）

### 8.1 Controller 方法骨架

```java
@PostMapping("/login")
@Operation(summary = "用户登录")
public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResult result = loginApplicationService.login(request.toCommand());
    return ApiResponse.ok(LoginResponse.from(result));
}
```

### 8.2 业务异常

```java
throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "用户名或密码错误");
```

### 8.3 VIP 惰性过期（必须模式）

```java
public VipStatus resolveVipStatus(Long userId) {
    UserVipPO vip = userVipMapper.selectActiveByUserId(userId);
    if (vip == null) {
        return VipStatus.none();
    }
    Instant now = Instant.now();
    if (vip.getExpireAt().isBefore(now)) {
        if (vip.getStatus() == VipStatusEnum.ACTIVE) {
            vipExpireService.markExpiredAsync(userId);
        }
        return VipStatus.none();
    }
    return VipStatus.from(vip);
}
```

---

## 9. 文档维护义务

- 架构变更 → 更新 `ARCHITECTURE.md` 并考虑新增 ADR。
- 模块变更 → 更新 `MODULE_STRUCTURE.md`。
- 对外契约变更 → 更新 `EMBEDDING_AND_INTEGRATION.md` 与 OpenAPI。
- 规约变更 → 同步主规约与对应分卷。

---

## 10. 分卷必读场景

| 任务类型 | 必读分卷 |
|----------|----------|
| 新建模块/改 POM | 01 |
| 新建 Service/配置类 | 02 |
| 任何新类 | 03 |
| 新 REST 接口 | 04, 05 |
| 新表/Mapper | 06, 先询问 |
| 登录/注册/VIP | 07, 05 |
| 公告功能 | 08 |
| 异常/日志 | 09 |
| 写测试 | 10 |
| 改 yml | 11 |
| 提交代码 | 12 |

---

*本规约随项目演进更新；冲突时以分卷细则与最新 ADR 为准。*
