# um-profile 模块

## 1. 定位

**用户资料应用层**：注册后初始化、查询、更新基础字段与 `ext_data` JSON 扩展。

## 2. 依赖关系

```
um-profile → um-infrastructure
```

被 `um-auth` 在注册流程中调用；被 `um-api` 的 `ProfileController` 暴露 HTTP。

## 3. 核心类

### ProfileApplicationService

| 方法 | 说明 | 事务 |
|------|------|------|
| `initProfile(userId, nickname)` | 插入 `sys_user_profile` 默认行 | 写 |
| `getProfile(userId)` | 按 userId 查询，不存在抛 `PROFILE_NOT_FOUND` | 只读 |
| `updateProfile(UpdateProfileCommand)` | 更新昵称/头像/性别/生日，合并 extPatch | 写 |

### UpdateProfileCommand

字段：`userId`, `nickname`, `avatarUrl`, `gender`, `birthday`, `extPatch`（Map）

**扩展字段规则**：`extPatch` 中与 `_` 开头的 key 会被忽略（保留系统内部键）。

## 4. HTTP 入口

由 `um-api` → `ProfileController`：

- `GET /api/v1/um/profile/me`
- `PUT /api/v1/um/profile/me`

## 5. 流程文档

[用户资料](../flows/用户资料.md)

## 6. 设计说明

- 资料表以 `user_id` 为主键，与账号 1:1。
- 高频变动、非结构化数据放入 `ext_data`，避免频繁 DDL。
- 乐观锁 `version` 字段在规约中预留，当前表结构若未加 `version` 列则后续 Flyway 增补。
