# um-bootstrap 模块

## 1. 定位

**可执行 Jar 聚合模块**：Spring Boot 启动、全局配置、Flyway 迁移、异步线程池。

## 2. 依赖关系

```
um-bootstrap → um-api（传递引入全部业务模块）
             → um-infrastructure
```

## 3. 入口类

`com.um.core.bootstrap.UmCoreApplication`

- `@SpringBootApplication(scanBasePackages = "com.um.core")`
- `@EnableAsync` — 支持 `VipExpireService.markExpiredAsync`

## 4. 配置

### application.yml（要点）

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| spring.datasource.* | `UM_DATASOURCE_*` | MySQL 连接 |
| spring.data.redis.* | `UM_REDIS_*` | Redis |
| um.auth.jwt-secret | `UM_JWT_SECRET` | JWT 密钥 |
| um.auth.geo-login-alert-enabled | — | 异地登录拦截，默认 false |
| server.port | `SERVER_PORT` | 默认 8080 |

### Flyway

- 路径：`src/main/resources/db/migration/`
- 启动时自动迁移

## 5. 异步线程池

`AsyncConfig` → Bean `umTaskExecutor`

供 VIP 过期异步落库等使用。

## 6. 打包与运行

```bash
# 开发启动（先编译依赖模块）
mvn spring-boot:run -pl um-bootstrap -am

# 或打包后运行
mvn clean package -pl um-bootstrap -am -DskipTests
java -jar um-bootstrap/target/um-bootstrap-1.0.0-SNAPSHOT.jar
```

> 父工程 `um-core` 为 `packaging=pom`，已在父 POM 对 `spring-boot-maven-plugin` 设置 `skip=true`，避免 `spring-boot:run` 在聚合模块上报错「找不到 main class」。

## 7. 健康检查

`GET /actuator/health` — 需 MySQL、Redis 可达时状态为 UP。
