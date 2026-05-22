# um-api 模块

## 1. 定位

**唯一 HTTP 入口**：REST Controller、请求/响应 DTO、Spring Security、全局异常、TraceId。

## 2. 依赖关系

```
um-api → um-auth, um-profile, um-vip, um-common, um-infrastructure
```

## 3. Controller 清单

| Controller | 基础路径 | 说明 |
|------------|----------|------|
| `AuthController` | `/api/v1/um/auth` | 注册、登录、短信、刷新、登出、注销 |
| `ProfileController` | `/api/v1/um/profile` | 资料查询与更新 |
| `VipController` | `/api/v1/um/vip` | VIP 查询与购买类操作 |

## 4. 安全组件

| 类 | 说明 |
|----|------|
| `SecurityConfig` | 白名单：login/register/refresh/sms/health/swagger；其余需认证 |
| `JwtAuthFilter` | 解析 Bearer Token，设置 `UserPrincipal` |
| `UserPrincipal` | 当前 userId、deviceId；`UserPrincipal.currentUserId()` |

### 白名单路径

- `POST /api/v1/um/auth/login`
- `POST /api/v1/um/auth/register/**`
- `POST /api/v1/um/auth/refresh`
- `POST /api/v1/um/auth/sms/send`
- `GET /actuator/health`

## 5. 横切能力

| 类 | 说明 |
|----|------|
| `TraceIdFilter` | 读取/生成 `X-Trace-Id`，写入 MDC 与响应头 |
| `GlobalExceptionHandler` | `BusinessException`、校验异常、未知异常 → `ApiResponse` |

## 6. 设计约束

- Controller **仅做**：`@Valid` 校验、Command 转换、调用 ApplicationService、包装 `ApiResponse`。
- **禁止**在本模块编写业务规则或直接调用 Mapper。

## 7. DTO 包结构

```
com.um.core.api.dto.auth.*      — 登录注册相关 Request/Response
com.um.core.api.dto.profile.*   — 资料
com.um.core.api.dto.vip.*       — VIP
```

## 8. API 文档

详见 [03-API-参考](../03-API-参考.md) 与 Swagger UI。
