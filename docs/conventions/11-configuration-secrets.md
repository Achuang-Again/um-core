# 11 - 配置与密钥规约

## 1. 配置文件

| 文件 | 用途 |
|------|------|
| application.yml | 通用默认 |
| application-dev.yml | 本地开发 |
| application-test.yml | 测试 |
| application-prod.yml | 生产（仅结构，值来自环境变量） |

激活：`spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`

## 2. 环境变量命名

前缀 **`UM_`**，嵌套用下划线：

| 变量 | 对应配置 |
|------|----------|
| UM_DATASOURCE_URL | spring.datasource.url |
| UM_DATASOURCE_USERNAME | spring.datasource.username |
| UM_DATASOURCE_PASSWORD | spring.datasource.password |
| UM_REDIS_HOST | spring.data.redis.host |
| UM_JWT_PRIVATE_KEY | um.auth.jwt.private-key |
| UM_API_KEYS | um.integration.api-keys |

Spring Boot  relaxed binding：

```yaml
um:
  auth:
    jwt:
      private-key: ${UM_JWT_PRIVATE_KEY}
```

## 3. 密钥管理

### 3.1 必须

- 密钥、密码 **仅** 通过环境变量或密钥管理服务注入。
- 仓库内 `application-dev.yml` 使用占位符或本地假值。
- `.gitignore` 包含 `.env`, `*.pem`, `secrets/`

### 3.2 禁止

- 将真实 JWT 私钥、数据库密码提交 Git。
- 在日志打印完整配置对象（`@ToString` 排除敏感字段）。

### 3.3 示例 application-dev.yml（安全）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/um_dev
    username: um_dev
    password: ${UM_DATASOURCE_PASSWORD:dev-only-password}

um:
  auth:
    jwt:
      private-key: ${UM_JWT_PRIVATE_KEY:classpath:dev-jwt-key.pem}
```

## 4. Redis 键前缀

全局前缀：`um:`

| 键模式 | TTL |
|--------|-----|
| um:refresh:{userId}:{deviceId} | 30d |
| um:token:blacklist:{jti} | access 剩余 |
| um:login:fail:{username} | 30min |
| um:sms:code:{phone} | 5min |
| um:idempotency:{key} | 24h |

## 5. 功能开关

```yaml
um:
  features:
    multi-tenant: true
    oauth-wechat: false
    geo-login-alert: true
```

使用 `@ConditionalOnProperty` 或配置类判断，避免硬编码。

## 6. 分布式锁（Redisson 可选）

场景：VIP 升级防并发

```
um:lock:vip:{userId}
```

持锁时间 ≤ 10s，必须 try-finally 释放。

## 7. 检查清单

- [ ] 无真实密钥在仓库
- [ ] 敏感项用 UM_* 环境变量
- [ ] Redis 键带 um: 前缀
- [ ] prod 配置无默认弱密码
