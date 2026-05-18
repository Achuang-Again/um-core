# 05 - 安全与认证规约

## 1. 威胁模型（简要）

| 威胁 | 缓解 |
|------|------|
| 密码泄露 | BCrypt、禁止日志打印 |
| Token 窃取 | 短 Access、HTTPS、HttpOnly 由宿主负责 |
| 暴力破解 | Redis 计数锁定 |
| CSRF | 无 Cookie 会话，Bearer Token |
| 越权 | 资源路径带 `/me` 或校验 userId 与 Principal 一致 |

## 2. 密码

### 2.1 必须

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

- 注册、改密：校验强度（长度 ≥ 8，含字母数字，可配置）。
- 存储：仅 password_hash，永不存明文。

### 2.2 禁止

- MD5/SHA1 自定义哈希。
- 日志输出 password、rawPassword。

## 3. JWT

### 3.1 Access Token Claims

| Claim | 说明 |
|-------|------|
| sub | userId |
| iss | `um-core` |
| exp | 过期时间 |
| tenant_id | 多租户（可选） |
| device_id | 设备（可选） |

- 有效期：默认 **2 小时**（`um.auth.access-token-ttl`）。
- 算法：RS256 或 HS256（生产推荐 RS256）。

### 3.2 Refresh Token

- 随机 UUID 或独立 JWT，存 Redis：`um:refresh:{userId}:{deviceId}` → token。
- 有效期：**30 天**。
- 刷新时 **轮换**：旧 refresh 失效，签发新对。

### 3.3 登出与踢人

- 登出：删除对应 refresh；Access 加入黑名单 `um:token:blacklist:{jti}` TTL=剩余有效期。
- 踢人：删除用户所有 `um:refresh:{userId}:*` 键。

## 4. 登录风控

Redis 键：`um:login:fail:{username}`

| 规则 | 默认 |
|------|------|
| 连续失败次数 | 5 |
| 锁定时长 | 30 分钟 |
| 异地 IP | 记录审计日志，可配置拒绝 |

## 5. 注册方式

| 方式 | 校验 |
|------|------|
| 用户名+密码 | 用户名唯一、密码强度 |
| 手机+验证码 | 验证码 `um:sms:code:{phone}` TTL 5min，单次使用 |
| 邮箱 | 邮件链接或验证码 |
| OAuth2 | state 防 CSRF；回调校验 code |

## 6. OAuth2

- state 存 Redis，回调校验后删除。
- 第三方 userId 映射表 `um_user_oauth`（provider, open_id, user_id）。
- 不将 OAuth access_token 返回给宿主前端。

## 7. API Key（服务间）

- Header：`X-Api-Key`
- 配置表或环境变量映射 `host_app_id`。
- 与租户组合校验权限范围。

## 8. 注销（Deactivate）

**必须**软删除 + 匿名化：

```sql
UPDATE um_user SET
  status = 'DEACTIVATED',
  deleted_at = NOW(),
  phone = NULL,
  email = NULL,
  phone_hash = SHA2(CONCAT(:salt, :original_phone), 256),
  username = CONCAT('deleted_', id),
  updated_at = NOW()
WHERE id = :userId;
```

- 吊销所有 Token。
- **禁止** `DELETE FROM um_user`。

## 9. Spring Security 路径

| 路径 | 权限 |
|------|------|
| POST /api/v1/um/auth/login | permitAll |
| POST /api/v1/um/auth/register | permitAll |
| POST /api/v1/um/auth/refresh | permitAll |
| GET /actuator/health | permitAll |
| GET /api/v1/um/announcements | permitAll 或 authenticated（按产品） |
| 其余 | authenticated |

## 10. 检查清单

- [ ] BCrypt 存储密码
- [ ] Token 不写日志
- [ ] 注销软删+匿名化
- [ ] Refresh 轮换
- [ ] OAuth state 校验
