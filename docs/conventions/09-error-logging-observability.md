# 09 - 异常、日志与可观测性规约

## 1. 异常体系

### 1.1 BusinessException

```java
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- 可预期业务失败 **必须** 抛 `BusinessException`，禁止用返回 null 表示错误。

### 1.2 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ApiResponse.fail(ErrorCodes.PARAM_INVALID, msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.fail(ErrorCodes.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}
```

- 未知异常 HTTP 500，message 不暴露堆栈给客户端。

## 2. 错误码管理

- 所有错误码定义在 `um-common` → `ErrorCodes`。
- 分段见主规约；新增码需更新 OpenAPI 描述（可选枚举文档）。

## 3. TraceId

- 入口过滤器从 Header `X-Trace-Id` 读取，无则生成 UUID。
- 存入 MDC：`traceId`。
- 写入 `ApiResponse.traceId` 与每条日志。

```java
MDC.put("traceId", traceId);
try {
    chain.doFilter(request, response);
} finally {
    MDC.clear();
}
```

## 4. 日志

### 4.1 级别

| 级别 | 用途 |
|------|------|
| ERROR | 需告警的异常、外部依赖失败 |
| WARN | 风控触发、重试、降级 |
| INFO | 登录成功/失败（不含密码）、VIP 变更 |
| DEBUG | 开发调试，生产默认关闭 |

### 4.2 禁止记录

- password、rawPassword
- accessToken、refreshToken 全文
- 短信/邮箱验证码
- API Key 全文（可记录后 4 位）

### 4.3 结构化（推荐）

```json
{
  "timestamp": "2026-05-17T10:00:00Z",
  "level": "INFO",
  "traceId": "abc",
  "userId": 123,
  "action": "LOGIN_SUCCESS",
  "ip": "1.2.3.4"
}
```

使用 Logback JSON encoder 或 `log.info("action=LOGIN_SUCCESS userId={}", userId)`。

## 5. Actuator

| 端点 | 暴露 |
|------|------|
| health | 内网 |
| info | 禁用或内网 |
| env/beans | 禁止公网 |

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
```

## 6. 指标（可选演进）

- Micrometer + Prometheus：`um_login_total`, `um_vip_subscribe_total`。
- 标签：`tenant_id`, `result`。

## 7. 检查清单

- [ ] 业务错误用 BusinessException
- [ ] 全局处理器覆盖校验异常
- [ ] 日志无敏感信息
- [ ] traceId 贯穿 MDC 与响应
