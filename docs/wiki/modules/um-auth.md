# um-auth 模块

## 1. 定位

**账号认证应用层**：注册、登录、登出、踢人、注销、短信验证码发送。

## 2. 依赖关系

```
um-auth → um-infrastructure
        → um-profile（注册后初始化资料）
        → um-vip（登录时 VIP 惰性检查）
```

## 3. ApplicationService 能力矩阵

| 服务类 | 核心方法 | 能力 |
|--------|----------|------|
| `RegisterApplicationService` | `registerByUsername` | 用户名注册 + 自动登录发 Token |
| | `registerByPhone` | 校验短信码、注册、发 Token |
| `LoginApplicationService` | `login` | 密码校验、风控、发 Token |
| | `refresh` | Refresh 轮换 |
| | `issueTokens` | 签发 Access + Refresh（内部复用） |
| `LogoutApplicationService` | `logout` | 吊销 Refresh、Access 加黑名单 |
| | `kickAllDevices` | 删除用户所有 `um:refresh:*` |
| `DeactivateApplicationService` | `deactivate` | 软注销 + 匿名化 + 全端登出 |
| `SmsCodeApplicationService` | `sendLoginCode` | 生成验证码存 Redis（开发环境打日志） |

## 4. 命令对象（Command）

| Command | 字段要点 |
|---------|----------|
| `RegisterByUsernameCommand` | username, password, clientIp |
| `RegisterByPhoneCommand` | phone, smsCode, password, clientIp |
| `LoginCommand` | principal（用户名或手机）, password, deviceId, clientIp |

## 5. 返回 DTO

`AuthTokenResult`：userId、accessToken、refreshToken、expiresIn、tokenType

## 6. 业务流程文档

- [注册与登录](../flows/注册与登录.md)
- [登出踢人与注销](../flows/登出踢人与注销.md)
- [Token 与安全](../flows/Token与安全.md)

## 7. 事务与异常

- 写操作：`@Transactional(rollbackFor = Exception.class)`
- 业务失败：抛 `BusinessException` + `ErrorCodes`（如 `AUTH_INVALID_CREDENTIALS`）

## 8. 待扩展

| 能力 | 状态 |
|------|------|
| 邮箱注册 | 未实现，可仿 `registerByPhone` |
| OAuth2 | 未实现，需 infrastructure 适配器 + 绑定表 |
