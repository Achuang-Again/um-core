# 01 - 项目与 Maven 规约

## 1. 根工程

| 属性 | 值 |
|------|-----|
| groupId | `com.um.core` |
| artifactId（父） | `um-core` |
| packaging | `pom` |
| Java | 21 |
| Maven | 3.9.16（本地构建环境） |
| 编码 | UTF-8 |

### 1.1 必须

- 根 `pom.xml` 使用 `<dependencyManagement>` 导入 `spring-boot-dependencies` BOM。
- 子模块版本继承父 POM `${project.version}`，子模块间依赖不写版本号。
- 插件版本在父 POM `<pluginManagement>` 统一声明：`maven-compiler-plugin`、`spring-boot-maven-plugin`、`flyway-maven-plugin`。

### 1.2 禁止

- 子模块单独指定 Spring Boot 版本（与 BOM 冲突）。
- 在 `um-domain` 引入 `spring-boot-starter-web`。
- 未经确认在父 POM `<dependencies>` 直接加依赖（应加 `dependencyManagement` + 子模块按需引用）。

## 2. 子模块 artifact 命名

| 模块目录 | artifactId |
|----------|------------|
| um-common | `um-common` |
| um-domain | `um-domain` |
| um-infrastructure | `um-infrastructure` |
| um-auth | `um-auth` |
| um-profile | `um-profile` |
| um-vip | `um-vip` |
| um-announcement | `um-announcement` |
| um-api | `um-api` |
| um-bootstrap | `um-bootstrap` |

## 3. 模块 pom 模板

```xml
<parent>
    <groupId>com.um.core</groupId>
    <artifactId>um-core</artifactId>
    <version>${revision}</version>
</parent>
<artifactId>um-auth</artifactId>
<dependencies>
    <dependency>
        <groupId>com.um.core</groupId>
        <artifactId>um-infrastructure</artifactId>
    </dependency>
</dependencies>
```

## 4. 依赖引入流程（AI 必读）

1. 确认是否已有 `um-common` 或 Spring 自带能力。
2. **先询问维护者**是否允许新增传递依赖。
3. 在父 POM `dependencyManagement` 声明版本。
4. 在目标子模块 `dependencies` 引用。
5. 运行 `mvn dependency:tree` 检查冲突。

## 5. 构建命令

| 场景 | 命令 |
|------|------|
| 全量测试 | `mvn clean test` |
| 打包可执行 Jar | `mvn clean package -pl um-bootstrap -am` |
| 单模块测试 | `mvn test -pl um-auth -am` |
| 跳过测试（仅本地调试，禁止提交前） | `mvn package -DskipTests` |

**提交前必须** `mvn clean test` 通过。

## 6. 目录与资源

| 内容 | 位置 |
|------|------|
| Flyway SQL | `um-bootstrap/src/main/resources/db/migration/` |
| Mapper XML | `um-infrastructure/src/main/resources/mapper/` |
| 主配置 | `um-bootstrap/src/main/resources/application.yml` |
| 文档 | 仓库根 `docs/` |

## 7. 版本属性（父 POM properties 示例）

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.2.5</spring-boot.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <revision>1.0.0-SNAPSHOT</revision>
</properties>
```

## 8. 检查清单

- [ ] 新类在正确子模块
- [ ] 无循环依赖
- [ ] 新依赖已登记 dependencyManagement
- [ ] 未改 CI 配置
