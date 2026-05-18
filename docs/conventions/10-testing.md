# 10 - 测试规约

## 1. 测试金字塔

| 层级 | 位置 | 比例建议 |
|------|------|----------|
| 单元测试 | 各模块 `src/test/java` | 70% |
| 集成测试 | infrastructure、api | 25% |
| 端到端 | `um-bootstrap` 或 api 包 | 5% |

## 2. 技术栈

- JUnit 5（Jupiter）
- Mockito 5
- AssertJ
- Spring Boot Test（`@WebMvcTest`, `@SpringBootTest`）
- Testcontainers MySQL（集成测）

**禁止** 新测试使用 JUnit 4。

## 3. 命名

```
{ClassName}Test
{ClassName}IT   // 集成测试可选后缀
```

方法：

```java
@Test
void login_shouldReturnToken_whenCredentialsValid() { }
```

模式：`{method}_should{Expected}_when{Condition}`。

## 4. 单元测试

### 4.1 ApplicationService

- Mock Repository、Redis、外部客户端。
- 覆盖：成功路径、业务异常、边界条件。

```java
@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks LoginApplicationService service;

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.login(cmd))
            .isInstanceOf(BusinessException.class)
            .extracting("code").isEqualTo(ErrorCodes.AUTH_INVALID_CREDENTIALS);
    }
}
```

### 4.2 Domain

- 无 Mock 框架依赖的纯逻辑测试（VIP 折算、extension 合并）。

## 5. 集成测试

```java
@Testcontainers
@SpringBootTest
class UserMapperIT {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
    }
}
```

- Flyway 自动迁移测试库。
- 测完清理数据或 @Transactional 回滚。

## 6. API 测试

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean LoginApplicationService loginApplicationService;

    @Test
    void login_returns200() throws Exception {
        when(loginApplicationService.login(any())).thenReturn(result);
        mockMvc.perform(post("/api/v1/um/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{...}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
```

## 7. 覆盖率

| 模块 | 行覆盖率目标 |
|------|--------------|
| um-auth, um-vip, um-profile | ≥ 80% |
| um-api Controller | ≥ 70% |
| um-infrastructure Mapper | 核心 SQL 有 IT |

不强制全局数字，但 **新增 Application 逻辑必须带测试**。

## 8. 测试数据

- 使用 Builder 或 fixture 类 `TestDataFactory`。
- 禁止依赖生产数据库。
- 密码测试用已知 BCrypt hash。

## 9. 提交前

```bash
mvn clean test
```

**必须** 全绿方可提交。

## 10. 检查清单

- [ ] JUnit 5
- [ ] 新 Service 有单元测试
- [ ] 复杂 SQL 有集成测试
- [ ] mvn clean test 通过
