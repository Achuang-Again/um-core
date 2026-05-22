# 03 - API 参考

Base URL 示例：`http://localhost:8080`  
统一前缀：`/api/v1/um`

## 1. 认证（无需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register/username` | 用户名密码注册 |
| POST | `/auth/register/phone` | 手机号+验证码注册 |
| POST | `/auth/sms/send` | 发送短信验证码 |
| POST | `/auth/login` | 登录 |
| POST | `/auth/refresh` | 刷新 Token |

### 登录请求示例

```json
{
  "principal": "alice",
  "password": "password123",
  "deviceId": "web-001"
}
```

### Token 响应

```json
{
  "code": 0,
  "data": {
    "userId": 1,
    "accessToken": "eyJ...",
    "refreshToken": "uuid",
    "expiresIn": 7200,
    "tokenType": "Bearer"
  }
}
```

## 2. 认证（需 Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/logout` | 退出当前设备 |
| POST | `/auth/kick-all` | 踢掉所有设备会话 |
| POST | `/auth/deactivate` | 注销账号 |

Header：`Authorization: Bearer <accessToken>`

## 3. 用户资料

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/profile/me` | 查询当前用户资料 |
| PUT | `/profile/me` | 更新资料（含 extPatch） |

## 4. VIP

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/vip/me` | 当前 VIP 状态（惰性过期） |
| POST | `/vip/subscribe` | 开通 |
| POST | `/vip/renew` | 续费 |
| POST | `/vip/upgrade` | 升级 |

写操作建议 Header：`Idempotency-Key: <uuid>`

### VIP 购买请求示例

```json
{
  "levelCode": "GOLD",
  "durationDays": 30,
  "orderNo": "PAY20260519001",
  "paidAmount": 79.90
}
```

## 5. 错误码分段

| 段位 | 示例 |
|------|------|
| 10001 | 参数错误 |
| 20001 | 用户名或密码错误 |
| 20002 | 账号锁定 |
| 30001 | 资料不存在 |
| 40001 | VIP 等级不存在 |
| 40002 | 订单重复 |

完整定义：`um-common` → `ErrorCodes.java`

## 6. 运维

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/actuator/health` | 健康检查 |

Swagger UI：`/swagger-ui.html`
