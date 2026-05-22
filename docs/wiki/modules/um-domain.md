# um-domain 模块

## 1. 定位

**纯净领域层**：枚举、值对象、不依赖 Spring/MyBatis 的领域算法（部分算法在 `um-vip.domain` 包）。

## 2. 依赖关系

- 依赖：`um-common`（异常/错误码引用可选）
- 被依赖：`um-infrastructure`、`um-vip`、`um-auth`（传递）

## 3. 核心能力

### 3.1 枚举

| 枚举 | 说明 |
|------|------|
| `UserStatusEnum` | 用户状态：DISABLED(0)、NORMAL(1)、DEACTIVATED(2) |
| `VipUserStatusEnum` | VIP 行状态：EXPIRED(0)、ACTIVE(1) |
| `VipActionTypeEnum` | 流水类型：首开/续费/升级/赠送 |
| `RegisterTypeEnum` | 注册方式：USERNAME、PHONE、EMAIL、OAUTH |

### 3.2 值对象

| 类 | 说明 |
|----|------|
| `VipStatus` | VIP 查询结果：`active`、`levelCode`、`expireTime` 等；`none()` 表示非会员 |

### 3.3 领域算法（um-vip 子包）

| 类 | 说明 |
|----|------|
| `VipUpgradeCalculator` | 升级时按剩余天数与月费比例折算新到期时间 |

## 4. 设计约束

- 禁止在本模块引入 `spring-*`、`mybatis-*` 依赖。
- 持久化 PO 放在 `um-infrastructure`，不放在此模块。
