# 02 - Java 21 与 Spring Boot 3 规约

## 1. Java 21

### 1.1 推荐使用

| 特性 | 用途 |
|------|------|
| `record` | REST DTO、不可变命令对象、值对象 |
| `sealed interface/class` | 错误类型、领域事件类型封闭 |
| `Optional` | 返回值可能为空的应用服务方法 |
| `var` | 局部变量类型明显时 |
| `Instant` / `ZoneOffset.UTC` | 时间戳存储与 API 序列化 |
| `Text Blocks` | SQL 片段、长字符串 |

### 1.2 避免

- `Date` / `Calendar`（使用 `java.time`）。
- 反射破坏不可变性。
- 空 `catch` 块。
- 在热路径滥用 `synchronized`（优先 Redis 分布式锁场景明确时使用）。

### 1.3 Record DTO 示例

```java
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password,
    @Size(max = 64) String deviceId
) {
    public LoginCommand toCommand() {
        return new LoginCommand(username, password, deviceId);
    }
}
```

## 2. Spring Boot 3

### 2.1 依赖注入

**必须**构造器注入：

```java
@Service
@RequiredArgsConstructor
public class LoginApplicationService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
}
```

**禁止** 字段 `@Autowired`（测试 `@MockBean` 除外）。

### 2.2 配置

- 使用 `@ConfigurationProperties(prefix = "um")` 绑定配置类 `UmProperties`。
- 配置类放在 `um-bootstrap` 或 `um-infrastructure.config`。
- 禁止 `@Value` 散落超过 3 处相同前缀。

```java
@ConfigurationProperties(prefix = "um.auth")
public record AuthProperties(
    Duration accessTokenTtl,
    Duration refreshTokenTtl,
    int maxLoginAttempts
) {}
```

### 2.3 事务

- 写操作：`@Transactional(rollbackFor = Exception.class)` 在 **ApplicationService** 层。
- 只读查询：`@Transactional(readOnly = true)`。
- 禁止在 Controller 上使用 `@Transactional`。

### 2.4 异步

VIP 过期落库、审计日志：

```java
@Async("umTaskExecutor")
public void markExpiredAsync(Long userId) { ... }
```

线程池在 `um-bootstrap` 统一配置 `ThreadPoolTaskExecutor` bean 名称 `umTaskExecutor`。

## 3. Spring Security 6

- 使用 `SecurityFilterChain` Bean，禁用默认表单登录（REST API）。
- 路径白名单：`/api/v1/um/auth/login`、`/register`、`/refresh`、`/actuator/health`。
- 其余 `/api/v1/um/**` 需认证。
- CSRF 对纯 API **禁用**；依赖 Token + HTTPS。

## 4. Validation

- Controller 参数：`@Valid` + Jakarta Validation。
- 自定义校验：注解放在 `um-common.validation`。
- 消息键：`{validation.user.username.notblank}`，资源文件 `messages_zh_CN.properties`。

## 5. Jackson

- 日期序列化：ISO-8601 字符串（`Instant`）。
- 禁止全局 `FAIL_ON_UNKNOWN_PROPERTIES=false` 在生产盲目忽略未知字段；DTO 应明确字段。
- `extension_json` 使用 `Map<String, Object>` 或 `JsonNode`。

## 6. 虚拟线程（可选）

Java 21 虚拟线程可用于高并发 I/O（如 OAuth HTTP 调用）：

```yaml
spring:
  threads:
    virtual:
      enabled: true  # 按需开启，需压测验证
```

默认不强制；开启需 ADR 记录。

## 7. 检查清单

- [ ] 构造器注入
- [ ] 无字段 @Autowired
- [ ] 事务在 Application 层
- [ ] 使用 java.time
- [ ] DTO 为 record + validation
