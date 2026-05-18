# 03 - 包结构与命名规约

## 1. 根包

```
com.um.core
```

## 2. 包路径模板

```
com.um.core.{module}.{layer}[.{feature}]
```

| module | 说明 |
|--------|------|
| common | 通用 |
| domain | 领域 |
| infrastructure | 基础设施 |
| auth, profile, vip, announcement | 业务 |
| api | REST 门面 |
| bootstrap | 启动（可选子包 config） |

| layer | 内容 |
|-------|------|
| api.controller | Controller（仅 um-api） |
| api.dto.request / response | REST DTO |
| application | ApplicationService |
| application.command / query | 命令查询对象 |
| domain.model | 实体、值对象 |
| domain.service | 领域服务接口 |
| domain.enums | 枚举 |
| infrastructure.persistence.mapper | Mapper |
| infrastructure.persistence.po | PO |
| infrastructure.persistence.converter | PO ↔ Domain |
| infrastructure.redis | Redis 仓储 |
| config | Spring 配置类 |

## 3. 类命名

| 类型 | 后缀/格式 | 示例 |
|------|-----------|------|
| Controller | `*Controller` | `AuthController` |
| ApplicationService | `*ApplicationService` | `LoginApplicationService` |
| Domain Service | `*DomainService` | `VipUpgradeDomainService` |
| Mapper | `*Mapper` | `UserMapper` |
| PO | `*PO` | `UserPO` |
| Converter | `*Converter` | `UserConverter` |
| DTO Request | `*Request` | `RegisterRequest` |
| DTO Response | `*Response` | `UserProfileResponse` |
| 命令 | `*Command` | `RegisterCommand` |
| 配置属性 | `*Properties` | `UmAuthProperties` |

## 4. 方法命名

| 操作 | 前缀 |
|------|------|
| 查询单个 | `get`, `find` |
| 查询列表 | `list`, `query` |
| 创建 | `create`, `register` |
| 更新 | `update` |
| 删除（逻辑） | `deactivate`, `remove`, `delete`（内部软删） |
| 布尔判断 | `is`, `has`, `can` |
| 领域解析 | `resolve` | `resolveVipStatus` |

## 5. 数据库命名

| 对象 | 规则 | 示例 |
|------|------|------|
| 表 | `um_{entity}` 蛇形 | `um_user` |
| 列 | 蛇形 | `expire_at`, `tenant_id` |
| 主键 | `id` 或 `{table}_id` | `id` |
| 索引 | `idx_{table}_{cols}` | `idx_um_user_phone` |
| 唯一键 | `uk_{table}_{cols}` | `uk_um_user_username` |

## 6. REST 路径命名

- 前缀：`/api/v1/um`
- 资源复数、小写、连字符：`/announcements`, `/vip-levels`
- 当前用户：`/profile/me`, `/vip/me`
- 动作型（认证）：`/auth/login`, `/auth/logout`

**禁止** 动词在路径中堆砌：`/getUserInfo` → 用 `GET /profile/me`。

## 7. 常量与枚举

```java
public enum VipStatusEnum {
    ACTIVE,
    EXPIRED,
    CANCELLED
}
```

错误码：

```java
public final class ErrorCodes {
    public static final int AUTH_INVALID_CREDENTIALS = 20001;
    private ErrorCodes() {}
}
```

## 8. 变量命名

- Java：camelCase
- 布尔：`enabled`, `locked`, `deleted`
- 集合复数：`users`, `announcements`
- 避免缩写：`pwd` → `password`

## 9. 检查清单

- [ ] 包路径含 module + layer
- [ ] PO 带 PO 后缀且仅在 infrastructure
- [ ] REST 路径符合 /api/v1/um 规范
- [ ] 表名 um_ 前缀
