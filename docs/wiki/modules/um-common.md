# um-common 模块

## 1. 定位

全项目最底层模块，**无 Spring 业务依赖**，提供跨模块共享的响应体、异常与工具类。

## 2. 依赖关系

- **被依赖**：`um-domain`、`um-api`（经传递或显式）
- **不依赖**：任何业务模块

## 3. 核心能力

| 类 | 能力 |
|----|------|
| `ApiResponse<T>` | 统一 REST 响应封装，`ok()` / `fail()` |
| `PageResult<T>` | 分页结果（预留） |
| `BusinessException` | 业务异常，携带 `code` |
| `ErrorCodes` | 错误码常量（1xxxx/2xxxx/3xxxx/4xxxx） |
| `TraceContext` | ThreadLocal traceId，供日志与响应 |

## 4. 设计要点

- `ApiResponse` 自动填充 `traceId`（由 `um-api` 的 `TraceIdFilter` 设置）与 `timestamp`。
- 新增错误码必须在本模块 `ErrorCodes` 集中定义，避免魔法数字散落。

## 5. 关键代码位置

```
um-common/src/main/java/com/um/core/common/
├── web/ApiResponse.java
├── exception/BusinessException.java
├── constant/ErrorCodes.java
└── trace/TraceContext.java
```
