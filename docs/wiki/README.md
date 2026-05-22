# UM-Core 项目 Wiki

本 Wiki 描述 **UM-Core 通用用户管理微服务** 的架构、模块能力、数据模型、API 与核心业务流程，供开发、运维与宿主系统对接方查阅。

> 编程规约与 AI 开发约束见上级目录：[../AI_PROGRAMMING_CONVENTIONS.md](../AI_PROGRAMMING_CONVENTIONS.md)

## 文档导航

| 文档 | 说明 |
|------|------|
| [00-项目总览](00-项目总览.md) | 定位、技术栈、部署形态 |
| [01-模块关系与分层](01-模块关系与分层.md) | 依赖图、分层职责、调用链 |
| [02-数据模型](02-数据模型.md) | `sys_*` 表结构与字段说明 |
| [03-API 参考](03-API-参考.md) | REST 接口清单与契约 |

### 模块详解

| 模块 | 文档 |
|------|------|
| um-common | [modules/um-common.md](modules/um-common.md) |
| um-domain | [modules/um-domain.md](modules/um-domain.md) |
| um-infrastructure | [modules/um-infrastructure.md](modules/um-infrastructure.md) |
| um-auth | [modules/um-auth.md](modules/um-auth.md) |
| um-profile | [modules/um-profile.md](modules/um-profile.md) |
| um-vip | [modules/um-vip.md](modules/um-vip.md) |
| um-api | [modules/um-api.md](modules/um-api.md) |
| um-bootstrap | [modules/um-bootstrap.md](modules/um-bootstrap.md) |

### 核心流程

| 流程 | 文档 |
|------|------|
| 注册与登录 | [flows/注册与登录.md](flows/注册与登录.md) |
| 登出、踢人、注销 | [flows/登出踢人与注销.md](flows/登出踢人与注销.md) |
| Token 与安全 | [flows/Token与安全.md](flows/Token与安全.md) |
| 用户资料 | [flows/用户资料.md](flows/用户资料.md) |
| VIP 开通与续费 | [flows/VIP开通与续费.md](flows/VIP开通与续费.md) |
| VIP 升级与过期 | [flows/VIP升级与过期.md](flows/VIP升级与过期.md) |

## 推荐阅读顺序

1. [00-项目总览](00-项目总览.md) → [01-模块关系与分层](01-模块关系与分层.md)
2. 按需阅读 `modules/` 中对应模块
3. 实现或对接时查阅 `flows/` 与 [03-API 参考](03-API-参考.md)
