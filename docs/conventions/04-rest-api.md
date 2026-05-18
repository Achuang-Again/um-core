# 04 - RESTful API 规约

## 1. URL 规范

| 项 | 规则 |
|----|------|
| 前缀 | `/api/v1/um` |
| 版本 | 路径中 `v1`；破坏性变更升 `v2` |
| 方法 | GET 查询、POST 创建/动作、PUT 全量更新、PATCH 部分更新、DELETE 慎用（优先 POST 动作） |

## 2. 统一响应

```java
public record ApiResponse<T>(
    int code,
    String message,
    T data,
    String traceId,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data, TraceContext.get(), Instant.now());
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, TraceContext.get(), Instant.now());
    }
}
```

### 2.1 HTTP 状态码映射

| 场景 | HTTP | body.code |
|------|------|-----------|
| 成功 | 200 | 0 |
| 参数错误 | 400 | 10001+ |
| 未认证 | 401 | 20010+ |
| 无权限 | 403 | 10003 |
| 资源不存在 | 404 | 10004 |
| 冲突/业务拒绝 | 409 | 域错误码 |
| 限流 | 429 | 10005 |
| 服务器错误 | 500 | 10000 |

## 3. 分页

请求：

```
GET /api/v1/um/announcements?page=1&size=20&sort=publishAt,desc
```

响应 `data`：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 100,
  "totalPages": 5
}
```

- `page` 从 1 开始。
- `size` 默认 20，最大 100。

## 4. Controller 规约

### 4.1 必须

```java
@RestController
@RequestMapping("/api/v1/um/auth")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {

    private final LoginApplicationService loginApplicationService;

    @PostMapping("/login")
    @Operation(summary = "登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(LoginResponse.from(loginApplicationService.login(request.toCommand())));
    }
}
```

### 4.2 禁止

- 在 Controller 写业务逻辑超过 5 行。
- 直接返回 PO 或 Domain 实体。
- 使用 `@ResponseStatus` 替代统一异常处理（除 204 无体场景）。

## 5. DTO 规约

- Request/Response 分离，放 `um-api`。
- 使用 `record` + Jakarta Validation。
- 敏感字段不出现在 Response（password、salt）。
- 提供静态工厂 `from(Domain)` 或 MapStruct 接口。

## 6. 幂等

Header：

```http
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

适用：`POST /vip/subscribe`, `POST /vip/renew`。

服务端：Redis `um:idempotency:{key}` TTL 24h 存响应摘要。

## 7. 错误码段位

| 段位 | 示例 |
|------|------|
| 10001 | 参数校验失败 |
| 20001 | 用户名或密码错误 |
| 20002 | 账号已锁定 |
| 30001 | 资料版本冲突 |
| 40001 | VIP 等级不存在 |
| 50001 | 公告不存在 |

## 8. Content-Type

- 请求/响应：`application/json; charset=utf-8`
- 文件上传（头像）：`multipart/form-data`，单独接口

## 9. OpenAPI

- 类级 `@Tag`，方法级 `@Operation`。
- 安全方案：`@SecurityRequirement(name = "bearerAuth")`。
- 生产可通过 `springdoc.api-docs.enabled=false` 关闭。

## 10. 检查清单

- [ ] 路径含 /api/v1/um
- [ ] 返回 ApiResponse
- [ ] DTO 校验注解完整
- [ ] OpenAPI 注解已加
- [ ] 分页参数合法范围校验
