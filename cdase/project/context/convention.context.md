>[Active] 约定生效中
>[Expired] 约定已过期

# Project Conventions

> **Purpose**: Global enforceable rules that apply to all artifacts and code

---

## Active Conventions

1. [Active] 所有文档和代码注释使用简体中文
2. [Active] 所有 API 必须在使用前注册到 API Registry
3. [Active] 禁止在没有显式批准的情况下跨 Feature 修改代码
4. [Active] 每个 Feature 必须有明确的所有者
5. [Active] 所有测试必须对应到明确的验收标准
6. [Active] **Java 包名规范**：基础包名统一为 `com.twelfth`（十二斋的英文），不使用 `tangxiaonuan`
7. [Active] **实体层设计规范**：Entity 层必须严格按照面向对象原则设计，遵循封装、继承、多态原则
8. [Active] **RESTful API 路径规范**：所有 RESTful API 统一放在 `/api` 目录下，格式为 `/api/v{version}/{resource}`
9. [Active] **分层架构规范**：代码严格分为 4 层：Controller 层（API）、Service 层（业务组织）、Entity 层（实体关系）、Persistence 层（数据访问）
10. [Active] **测试策略规范**：关键测试必须覆盖 Entity 层，可 mock 所有持久化服务；各层测试必须独立覆盖（Entity/Persistence/Service/Controller）；Controller 层测试为端到端全量测试（不 mock）
11. [Active] **数据库选型与测试策略**：生产环境使用 PostgreSQL；Persistence 层测试可使用 H2 内存数据库（提速）；Controller E2E 测试使用真实 PostgreSQL（TestContainers）；需注意 SQL 语法兼容性
12. [Active] **Git 操作规范**：禁止在用户没有明确给出指令的情况下执行 `git commit` 或 `git push` 操作；所有代码提交必须由用户显式请求
13. [Active] **Android 技术栈规范**：客户端使用 Kotlin + Jetpack 架构；本地存储使用 SharedPreferences；登录方式为手机号+验证码（P1）；支付方式为支付宝+微信支付（P1）

---

## Expired Conventions

_(暂无)_

---

## 详细说明

### Convention 6: Java 包名规范
- **基础包名**: `com.twelfth`（代表"十二斋"）
- **模块包**: `com.twelfth.{module}` (如：`com.twelfth.user`, `com.twelfth.glucose`)
- **分层包**:
  - Controller: `com.twelfth.{module}.controller`
  - Service: `com.twelfth.{module}.service`
  - Entity: `com.twelfth.{module}.entity`
  - Mapper: `com.twelfth.{module}.mapper`
  - DTO: `com.twelfth.{module}.dto`

### Convention 7: Entity 层面向对象设计规范
- Entity 类必须具备完整的封装性（私有字段 + Getter/Setter）
- 合理使用继承（如：BaseEntity 包含通用字段 id, createdAt, updatedAt）
- Entity 之间的关联关系通过对象引用表达（避免仅使用 ID 字段）
- Entity 应包含必要的业务方法（不只是数据容器）
- 使用 JPA/Hibernate 注解规范（@Entity, @Table, @Id, @Column 等）

### Convention 8: RESTful API 路径规范
- 统一前缀: `/api/v1/`（v1 表示 API 版本）
- 资源命名: 使用复数名词（如：`/api/v1/users`, `/api/v1/glucose/records`）
- 嵌套资源: 最多两层（如：`/api/v1/users/{userId}/medical-history`）
- 避免动词: 使用 HTTP 方法表达操作（GET/POST/PUT/DELETE）

### Convention 9: 分层架构规范

**四层架构体系**：

```
┌─────────────────────────────────────────┐
│    Controller 层（API 接口层）            │
│  - 接收 HTTP 请求                        │
│  - 参数验证与转换                        │
│  - 调用 Service 层                       │
│  - 返回 HTTP 响应                        │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│    Service 层（业务逻辑组织层）           │
│  - 组织业务流程                          │
│  - 事务控制                              │
│  - 调用 Entity 层业务方法                │
│  - 调用 Persistence 层获取数据            │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│    Entity 层（实体关系层）                │
│  - 业务实体定义                          │
│  - 实体关联关系                          │
│  - 核心业务方法                          │
│  - 业务规则验证                          │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│    Persistence 层（数据持久化层）         │
│  - 数据库访问（Mapper/Repository）        │
│  - SQL/ORM 操作                          │
│  - 数据查询与持久化                       │
└─────────────────────────────────────────┘
```

**各层职责**：

1. **Controller 层**：
   - 仅处理 HTTP 协议相关逻辑
   - 不包含业务逻辑
   - 不直接访问 Persistence 层

2. **Service 层**：
   - 组织和编排业务流程
   - 负责事务边界定义（@Transactional）
   - 可调用多个 Entity 和 Persistence 层

3. **Entity 层**：
   - 包含业务实体和实体关系
   - 实现核心业务方法和验证逻辑
   - 可独立于持久化层进行测试

4. **Persistence 层**：
   - 仅负责数据访问
   - 不包含业务逻辑
   - 使用 MyBatis Mapper 或 JPA Repository

**依赖规则**：
- Controller → Service → Entity + Persistence
- Controller 不能直接调用 Persistence
- Entity 不能依赖 Persistence
- Persistence 不能包含业务逻辑

### Convention 10: 测试策略规范

**测试金字塔**：

```
        ┌─────────────┐
        │ Controller  │  ← E2E 全量测试（不 mock）
        │   层测试    │     使用真实 PostgreSQL
        ├─────────────┤
        │  Service    │  ← 集成测试（mock Persistence）
        │   层测试    │
        ├─────────────┤
        │   Entity    │  ← 单元测试（重点）⭐
        │   层测试    │     关键测试覆盖
        ├─────────────┤
        │ Persistence │  ← 集成测试（真实 PostgreSQL）
        │   层测试    │     使用 TestContainers
        └─────────────┘
```

**测试原则**：

1. **Entity 层测试（关键）**：
   - ✅ **必须覆盖**所有业务方法和验证逻辑
   - ✅ 可以 mock 掉所有 Persistence 服务
   - ✅ 测试业务规则和实体关联关系
   - 示例：`UserEntityTest.java`, `GlucoseRecordEntityTest.java`

2. **Persistence 层测试（快速测试）**：
   - 测试数据访问逻辑（CRUD）
   - **使用 H2 内存数据库（快速启动）**
   - 验证 SQL 正确性和数据一致性
   - ⚠️ 注意：SQL 需兼容 H2 和 PostgreSQL
   - 示例：`UserMapperTest.java`, `GlucoseRecordMapperTest.java`

3. **Service 层测试**：
   - 测试业务流程编排
   - Mock Persistence 层依赖
   - 验证事务边界和异常处理
   - 示例：`UserServiceTest.java`, `GlucoseServiceTest.java`

4. **Controller 层测试（E2E 全量测试）**：
   - **端到端测试，不 mock 任何层**
   - 启动完整 Spring Boot 应用（@SpringBootTest）
   - **使用真实 PostgreSQL 数据库（TestContainers）**
   - 验证完整请求-响应流程和数据持久化
   - 测试真实的业务场景和 SQL 兼容性
   - 示例：`UserControllerE2ETest.java`, `GlucoseControllerE2ETest.java`

**测试文件组织**：

```
src/test/java/com/twelfth/
├── user/
│   ├── entity/
│   │   ├── UserEntityTest.java           # Entity 层测试 ⭐
│   │   └── MedicalHistoryEntityTest.java
│   ├── mapper/
│   │   └── UserMapperTest.java           # Persistence 层测试
│   ├── service/
│   │   └── UserServiceTest.java          # Service 层测试
│   └── controller/
│       └── UserControllerTest.java       # Controller 层测试
├── glucose/
│   ├── entity/
│   │   └── GlucoseRecordEntityTest.java  # Entity 层测试 ⭐
│   ├── mapper/
│   │   └── GlucoseRecordMapperTest.java
│   ├── service/
│   │   └── GlucoseServiceTest.java
│   └── controller/
│       └── GlucoseControllerTest.java
└── ...
```

**Mock 策略**：
- Entity 测试：不 mock（纯业务逻辑测试）
- **Persistence 测试：不 mock（使用 H2 内存数据库，快速）**
- Service 测试：Mock Persistence 层
- **Controller 测试：不 mock（E2E 测试，使用真实 PostgreSQL）**

### Convention 11: 数据库选型与测试策略

**生产环境**：
- 数据库：**PostgreSQL 15+**
- ORM：MyBatis-Plus
- 连接池：HikariCP

**测试环境（分层策略）**：

| 测试层 | 数据库 | 原因 | 配置 |
|--------|--------|------|------|
| **Persistence 层** | **H2 内存数据库** | 快速启动，提高测试效率 | `@AutoConfigureTestDatabase` |
| **Controller E2E** | **PostgreSQL (TestContainers)** | 真实环境，验证 SQL 兼容性 | `@Testcontainers` |

**SQL 兼容性注意事项**：

⚠️ **必须遵守的 SQL 规范**（确保 H2 和 PostgreSQL 兼容）：

1. **字符串函数**：
   - ✅ 使用标准 SQL：`UPPER()`, `LOWER()`, `SUBSTRING()`
   - ❌ 避免特定函数：PostgreSQL 的 `STRING_AGG()`，H2 的 `GROUP_CONCAT()`

2. **日期时间**：
   - ✅ 使用 `CURRENT_TIMESTAMP`
   - ❌ 避免 `NOW()`（PostgreSQL 特有）

3. **JSON 操作**：
   - ✅ 简单 JSONB 字段可用
   - ⚠️ 复杂 JSON 操作需在 Controller E2E 测试中验证

4. **序列/自增**：
   - ✅ 使用 JPA `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - ✅ 或使用 UUID（推荐）

5. **索引和约束**：
   - ✅ 在 Entity 注解中定义
   - ⚠️ 复杂索引在 Controller E2E 测试中验证

**Persistence 层测试配置（H2）**：
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserMapperTest {
    // 快速测试 CRUD 逻辑
}
```

**Controller E2E 测试配置（PostgreSQL）**：
```java
@Testcontainers
@SpringBootTest
class UserControllerE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // 容器复用
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    // 真实环境端到端测试
}
```

### Convention 12: Git 操作规范

**核心原则**：所有代码提交必须由用户显式控制。

**禁止的操作**：
- ❌ AI 自动执行 `git commit`
- ❌ AI 自动执行 `git push`
- ❌ AI 自动执行 `git commit && git push`

**允许的操作**：
- ✅ AI 可以执行 `git status` 检查状态
- ✅ AI 可以执行 `git diff` 查看变更
- ✅ AI 可以执行 `git add` 暂存文件（如果用户明确要求）
- ✅ 用户明确说"提交代码"、"commit"、"push"时才能执行

**执行规则**：
1. AI 完成代码修改后，应报告修改内容，但**不执行 commit**
2. 用户需要明确说出：
   - "提交代码" / "commit 代码"
   - "推送到远程" / "push"
   - 或其他明确的提交指令
3. 只有在用户明确指令后，AI 才能执行 `git commit` 和 `git push`

**示例对话**：
```
❌ 错误流程：
User: "帮我修复这个 bug"
AI: [修复代码] "已修复，代码已提交并推送到远程"  ← 禁止！

✅ 正确流程：
User: "帮我修复这个 bug"
AI: [修复代码] "已完成修复，修改了以下文件：... 如需提交，请告诉我。"
User: "commit 并 push"
AI: [执行 git commit 和 git push] "已提交并推送到远程"
```

### Convention 13: Android 技术栈规范

**平台定位**：糖小暖是一款 **Android 原生应用**（非微信小程序）

**技术栈**：

1. **客户端开发**：
   - 开发语言：**Kotlin**
   - 架构组件：**Jetpack** (ViewModel, LiveData, Room, Navigation, etc.)
   - UI 框架：Jetpack Compose（推荐）或 XML Layouts
   - 异步处理：Kotlin Coroutines + Flow
   - 网络请求：Retrofit + OkHttp
   - 依赖注入：Hilt (Dagger)

2. **本地存储方案**：
   - **SharedPreferences**（用于访客ID、用户配置等轻量级数据）
   - Room Database（用于本地缓存、离线数据）
   - 文件存储：Android File System（用于图片、文档等）

3. **用户认证（版本规划）**：
   - **P0（当前版本）**：仅支持**访客模式**（Guest ID，存储在 SharedPreferences）
   - **P1（下个版本）**：**手机号 + 验证码**登录（SMS OTP）
   - P2（未来版本）：支持第三方登录（微信开放平台、Google 登录等）

4. **支付方式（版本规划）**：
   - **P0（当前版本）**：不支持支付功能
   - **P1（未来版本）**：集成**支付宝 SDK** + **微信支付 SDK**（Android 版本）
   - 需要服务端配合处理支付回调和订单状态

5. **代码组织结构**：
   ```
   com.twelfth.tangxiaonuan/
   ├── data/                    # 数据层
   │   ├── local/              # 本地存储（SharedPreferences, Room）
   │   ├── remote/             # 网络请求（Retrofit API）
   │   └── repository/         # Repository 模式
   ├── domain/                  # 业务逻辑层
   │   ├── model/              # 领域模型
   │   └── usecase/            # 用例
   ├── presentation/            # 展示层
   │   ├── ui/                 # UI 组件（Activity, Fragment, Compose）
   │   └── viewmodel/          # ViewModel
   └── di/                      # 依赖注入模块
   ```

**与后端的交互**：
- 所有 REST API 统一使用 `/api/v1/` 前缀
- Android 客户端通过 Retrofit 调用后端 API
- 访客ID通过 HTTP Header 或 Body 传递给后端

**本地存储示例**（访客ID）：
```kotlin
// 存储访客ID
val sharedPrefs = context.getSharedPreferences("tangxiaonuan_prefs", Context.MODE_PRIVATE)
sharedPrefs.edit().putString("GUEST_ID", guestId).apply()

// 读取访客ID
val guestId = sharedPrefs.getString("GUEST_ID", null)
```

---

## Convention Management

- 新约定必须标记为 [Active] 并需要用户确认
- 过期的约定必须移至 Expired Conventions 部分
- 约定违规将触发 STOP 并报告

---

**Last Updated**: 2026-01-30 10:15  
**Updated By**: hangxiao  
**Total Conventions**: 13 条活跃约定  
**SQL 兼容性**: 必须兼容 H2 (PostgreSQL 模式) 和 PostgreSQL  
**Git 控制**: 禁止 AI 自动提交或推送代码  
**平台**: Android 原生应用（Kotlin + Jetpack）

