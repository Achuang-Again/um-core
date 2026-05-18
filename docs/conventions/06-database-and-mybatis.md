# 06 - 数据库与 MyBatis-Plus 规约

## 1. 数据库

- 引擎：MySQL 8.0+
- 字符集：`utf8mb4`，排序规则 `utf8mb4_unicode_ci`
- 时区：存储 UTC（`DATETIME(3)` 或 `TIMESTAMP` 统一策略），应用层 `Instant`

## 2. 表设计通则

### 2.1 审计字段（必须）

| 列 | 类型 | 说明 |
|----|------|------|
| created_at | DATETIME(3) | 创建时间 |
| updated_at | DATETIME(3) | 更新时间 |
| created_by | BIGINT NULL | 操作人 |
| updated_by | BIGINT NULL | 操作人 |

### 2.2 多租户（可选）

| 列 | 说明 |
|----|------|
| tenant_id | BIGINT NOT NULL，复合唯一索引首列 |

### 2.3 软删除

- 用户主表：`deleted_at DATETIME(3) NULL`
- 或 MyBatis-Plus `@TableLogic` 字段 `deleted TINYINT`

**同一表只选一种策略，项目内统一。**

## 3. 核心表（参考）

### um_user

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 雪花 ID |
| tenant_id | BIGINT | 租户 |
| username | VARCHAR(64) | 唯一（未删除） |
| password_hash | VARCHAR(128) | BCrypt |
| phone | VARCHAR(20) | 可空 |
| email | VARCHAR(128) | 可空 |
| status | VARCHAR(32) | ACTIVE/LOCKED/DEACTIVATED |
| deleted_at | DATETIME(3) | 软删 |

### um_user_profile

| 列 | 类型 | 说明 |
|----|------|------|
| user_id | BIGINT PK/FK | |
| nickname | VARCHAR(64) | |
| avatar_url | VARCHAR(512) | |
| gender | TINYINT | 0未知1男2女 |
| extension_json | JSON | 扩展字段 |
| version | INT | 乐观锁 |

### um_vip_level

| 列 | 类型 |
|----|------|
| id | BIGINT PK |
| code | VARCHAR(32) UK |
| name | VARCHAR(64) |
| weight | INT |
| benefits_json | JSON |

### um_user_vip

| 列 | 类型 |
|----|------|
| user_id | BIGINT UK |
| level_id | BIGINT FK |
| expire_at | DATETIME(3) |
| status | VARCHAR(32) |

### um_announcement

见分卷 08。

## 4. Flyway

- 命名：`V{version}__{description}.sql`，如 `V1__init_um_user.sql`
- **变更前询问维护者**
- 禁止修改已发布版本的脚本；只能新增版本
- 回滚：提供补偿脚本 `V{n+1}__rollback_xxx.sql` 或手动方案文档

## 5. MyBatis-Plus

### 5.1 PO 示例

```java
@TableName("um_user")
public class UserPO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String passwordHash;
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
```

### 5.2 必须

- PO 仅存在于 `um-infrastructure`。
- Mapper 接口 `extends BaseMapper<UserPO>`。
- 复杂 SQL 写 XML，命名空间与接口全限定名一致。
- 查询明确列名，**禁止**生产 `SELECT *`。

### 5.3 Converter

```java
public final class UserConverter {
    public static User toDomain(UserPO po) { ... }
    public static UserPO toPO(User domain) { ... }
}
```

### 5.4 分页

```java
Page<UserPO> page = new Page<>(pageNum, pageSize);
userMapper.selectPage(page, wrapper);
```

## 6. 索引规范

- WHERE/ORDER BY 高频列建索引。
- 唯一索引考虑 `deleted_at` 或 `status`（软删下 username 唯一可用函数索引或业务层校验）。
- 避免过多联合索引超过 5 列。

## 7. 匿名化 SQL 模板

见 [05-security-and-auth.md](./05-security-and-auth.md) 注销章节。

## 8. JSON 字段

- `extension_json`、`benefits_json` 使用 MySQL `JSON` 类型。
- 应用层校验 JSON 大小 ≤ 64KB。
- 禁止存储循环引用对象。

## 9. 检查清单

- [ ] 新表 um_ 前缀
- [ ] Flyway 版本新增（已审批）
- [ ] PO/Mapper 在 infrastructure
- [ ] 无 SELECT *
- [ ] 审计字段齐全
