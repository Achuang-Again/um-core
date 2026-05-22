# um-vip 模块

## 1. 定位

**VIP 会员应用层**：等级查询、开通、续费、升级、惰性过期、流水记录。

## 2. 依赖关系

```
um-vip → um-infrastructure
```

被 `um-auth`（登录）、`um-api`（VIP API）调用。

## 3. 核心类

| 类 | 职责 |
|----|------|
| `VipApplicationService` | subscribe / renew / upgrade / getMyVip |
| `VipStatusResolver` | **惰性求值**解析当前是否 VIP |
| `VipExpireService` | `@Async` 将过期记录 status 置为 EXPIRED |
| `VipUpgradeCalculator` | 升级剩余时间折算（纯函数，在 domain 包） |

## 4. VipApplicationService 方法

| 方法 | action_type | 说明 |
|------|-------------|------|
| `subscribe` | 1 | 首开：`expire = now + durationDays` |
| `renew` | 2 | 续费：`expire = max(expire, now) + durationDays` |
| `upgrade` | 3 | 升级：折算 + purchasedDays，校验 weight 升高 |
| `getMyVip` | — | 委托 `VipStatusResolver.resolve` |

幂等：`IdempotencyService` + `order_no` 唯一约束。

## 5. VipStatusResolver（核心）

```
读取 sys_user_vip
  → 无记录：返回 VipStatus.none()
  → expire_time < now：
        若 status=ACTIVE → markExpiredAsync（不阻塞）
        返回 VipStatus.none()
  → 否则：联查 sys_vip_level，返回 active 状态
```

**禁止** Cron 全表扫描降级。

## 6. 流程文档

- [VIP 开通与续费](../flows/VIP开通与续费.md)
- [VIP 升级与过期](../flows/VIP升级与过期.md)

## 7. 单元测试

- `VipUpgradeCalculatorTest` — 折算算法
- `VipStatusResolverTest` — 惰性过期与有效 VIP
