# um-infrastructure 模块

## 1. 定位

**基础设施层**：数据库访问、Redis、JWT、密码编码器、JSON 扩展字段工具。

## 2. 依赖关系

- 依赖：`um-domain`
- 被依赖：`um-profile`、`um-vip`、`um-auth`、`um-api`、`um-bootstrap`

## 3. 核心能力一览

### 3.1 持久化（MyBatis-Plus）

| PO | 表 | Mapper |
|----|-----|--------|
| `SysUserPO` | sys_user | `SysUserMapper` |
| `SysUserProfilePO` | sys_user_profile | `SysUserProfileMapper` |
| `SysVipLevelPO` | sys_vip_level | `SysVipLevelMapper` |
| `SysUserVipPO` | sys_user_vip | `SysUserVipMapper` |
| `SysVipOrderPO` | sys_vip_order | `SysVipOrderMapper` |

仓储封装：

- `UserRepository` — 按用户名/手机/邮箱查询、保存用户

逻辑删除字段：`is_deleted`，全局配置见 `application.yml` 中 `mybatis-plus.global-config`。

### 3.2 Redis 服务

| 服务 | Redis 键模式 | 用途 |
|------|--------------|------|
| `TokenStoreService` | `um:refresh:{userId}:{deviceId}` | Refresh Token |
| | `um:token:blacklist:{jti}` | Access 黑名单（登出） |
| `LoginLockService` | `um:login:fail:{username}` | 连续登录失败计数 |
| `SmsCodeService` | `um:sms:code:{phone}` | 短信验证码，5 分钟 TTL |
| `IdempotencyService` | `um:idempotency:{key}` | VIP 写操作幂等，24h |

### 3.3 安全

| 类 | 能力 |
|----|------|
| `JwtTokenService` | 签发/解析 JWT Access（HS256，claims: sub=userId, device_id, jti） |
| `UmAuthProperties` | `um.auth.*` 配置：TTL、锁定次数、异地开关 |
| `PasswordEncoder` Bean | BCrypt strength 12 |

### 3.4 工具

| 类 | 能力 |
|----|------|
| `JsonExtHelper` | 读写 `ext_data` JSON，合并 key-value |

## 4. 自动配置

`InfrastructureAutoConfiguration`：

- `@MapperScan("com.um.core.infrastructure.persistence.mapper")`
- `@EnableConfigurationProperties(UmAuthProperties.class)`

## 5. 扩展点

- **OAuth 客户端**：可在 `infrastructure.oauth` 包新增适配器（尚未实现）
- **短信网关**：`SmsCodeService` 当前仅写 Redis，生产需对接运营商 API
