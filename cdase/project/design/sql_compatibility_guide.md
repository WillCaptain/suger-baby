# SQL 兼容性指南（H2 ↔ PostgreSQL）

> **Authority**: Convention §11  
> **Purpose**: 确保 SQL 在 H2（Persistence 测试）和 PostgreSQL（生产/E2E 测试）间兼容  
> **Last Updated**: 2026-01-29

---

## 1. 测试数据库策略

| 测试层 | 数据库 | 原因 |
|--------|--------|------|
| **Persistence 层** | **H2 (PostgreSQL 模式)** | ⚡ 快速启动（毫秒级），提高测试效率 |
| **Controller E2E** | **PostgreSQL (TestContainers)** | ✅ 真实环境，验证 SQL 兼容性和性能 |

---

## 2. H2 PostgreSQL 模式配置

### 2.1 application-test.yml（Persistence 测试）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect  # 使用 PostgreSQL 方言
```

**关键配置说明**：
- `MODE=PostgreSQL`：H2 启用 PostgreSQL 兼容模式
- `DATABASE_TO_LOWER=TRUE`：表名和列名转小写（与 PostgreSQL 一致）
- `DB_CLOSE_DELAY=-1`：测试期间保持数据库连接
- `dialect: PostgreSQLDialect`：使用 PostgreSQL 方言生成 SQL

---

## 3. SQL 兼容性规范

### 3.1 ✅ 推荐使用（兼容）

#### 数据类型
```sql
-- ✅ 基本类型（完全兼容）
VARCHAR(255)
INTEGER
BIGINT
DECIMAL(10, 2)
BOOLEAN
DATE
TIMESTAMP
TEXT

-- ✅ JSON 类型（H2 5.2+ 支持）
JSONB  -- 推荐使用，H2 和 PostgreSQL 都支持
```

#### 字符串函数
```sql
-- ✅ 标准 SQL 函数
UPPER(column_name)
LOWER(column_name)
SUBSTRING(column_name, start, length)
CONCAT(str1, str2)
LENGTH(column_name)
TRIM(column_name)
```

#### 日期时间函数
```sql
-- ✅ 标准函数
CURRENT_TIMESTAMP
CURRENT_DATE
CURRENT_TIME
EXTRACT(YEAR FROM date_column)
```

#### 聚合函数
```sql
-- ✅ 标准聚合
COUNT(*)
SUM(column_name)
AVG(column_name)
MAX(column_name)
MIN(column_name)
```

#### 窗口函数
```sql
-- ✅ 基本窗口函数（H2 2.x+ 支持）
ROW_NUMBER() OVER (PARTITION BY ... ORDER BY ...)
RANK() OVER (...)
DENSE_RANK() OVER (...)
```

---

### 3.2 ⚠️ 谨慎使用（需测试验证）

#### JSONB 操作符
```sql
-- ⚠️ PostgreSQL 特有操作符，H2 支持有限
-- 简单查询可用
SELECT * FROM users WHERE metadata::jsonb @> '{"status": "active"}'::jsonb;

-- ⚠️ 复杂 JSONB 操作需在 Controller E2E 测试验证
-- 推荐：在 Service 层处理 JSON，不在 SQL 中
```

#### 数组类型
```sql
-- ⚠️ PostgreSQL 数组，H2 支持有限
-- 推荐：使用 JSONB 代替数组
```

#### 全文搜索
```sql
-- ⚠️ PostgreSQL 特有
-- 推荐：Persistence 层测试跳过，Controller E2E 测试验证
```

---

### 3.3 ❌ 禁止使用（不兼容）

#### PostgreSQL 特有函数
```sql
-- ❌ 不要使用
NOW()                  -- 使用 CURRENT_TIMESTAMP
STRING_AGG()           -- 使用标准 GROUP_CONCAT() 或在代码中处理
ARRAY_AGG()            -- 使用 JSONB 或在代码中处理
TO_CHAR()              -- 使用 Java 格式化
GENERATE_SERIES()      -- 在代码中生成
```

#### H2 特有函数
```sql
-- ❌ 不要使用
GROUP_CONCAT()         -- PostgreSQL 不支持，使用 STRING_AGG() 或代码处理
CSVREAD()              -- H2 特有
```

#### 非标准语法
```sql
-- ❌ PostgreSQL 特有语法
SELECT * FROM table WHERE column ILIKE '%pattern%';  -- 使用 LOWER() 代替
SELECT * FROM table WHERE column ~ 'regex';          -- 避免正则表达式

-- ❌ H2 特有语法
SELECT TOP 10 * FROM table;  -- 使用标准 LIMIT
```

---

## 4. 实践建议

### 4.1 Entity 注解（推荐方式）

```java
@Entity
@Table(name = "tbl_user", indexes = {
    @Index(name = "idx_user_phone_hash", columnList = "phone_hash"),
    @Index(name = "idx_user_openid_hash", columnList = "wechat_openid_hash")
})
public class User extends BaseEntity {
    
    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId;
    
    @Enumerated(EnumType.STRING)  // ✅ 使用枚举字符串，兼容性好
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;
    
    @Column(name = "encrypted_phone", length = 200)
    private String encryptedPhone;
    
    @Column(name = "phone_hash", length = 64)
    private String phoneHash;  // ✅ 使用哈希索引，兼容性好
    
    @Type(JsonBinaryType.class)  // ✅ JSONB 类型
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ✅ 使用 Java 时间类型
}
```

### 4.2 MyBatis Mapper（SQL 编写）

```xml
<mapper namespace="com.twelfth.user.mapper.UserMapper">
    
    <!-- ✅ 推荐：使用标准 SQL -->
    <select id="selectByUserId" resultType="User">
        SELECT *
        FROM tbl_user
        WHERE user_id = #{userId}
          AND is_deleted = false
    </select>
    
    <!-- ✅ 推荐：使用参数化查询 -->
    <select id="selectByPhoneHash" resultType="User">
        SELECT *
        FROM tbl_user
        WHERE phone_hash = #{phoneHash}
          AND user_type = 'NORMAL'
        ORDER BY created_at DESC
        LIMIT 1
    </select>
    
    <!-- ⚠️ 谨慎：复杂 JSONB 查询，需在 E2E 测试验证 -->
    <select id="selectByMetadata" resultType="User">
        SELECT *
        FROM tbl_user
        WHERE metadata::jsonb @> #{metadataJson}::jsonb
    </select>
    
    <!-- ✅ 推荐：聚合统计 -->
    <select id="countByUserType" resultType="long">
        SELECT COUNT(*)
        FROM tbl_user
        WHERE user_type = #{userType}
          AND is_deleted = false
    </select>
</mapper>
```

### 4.3 复杂查询处理策略

```java
// ❌ 不推荐：在 SQL 中处理复杂 JSON
@Select("SELECT * FROM users WHERE metadata->'settings'->>'language' = 'zh-CN'")
List<User> findByLanguage();

// ✅ 推荐：在 Java 代码中处理
@Select("SELECT * FROM users WHERE metadata IS NOT NULL")
List<User> findAllWithMetadata();

// 在 Service 层过滤
public List<User> findByLanguage(String language) {
    List<User> users = userMapper.findAllWithMetadata();
    return users.stream()
        .filter(u -> language.equals(u.getMetadata().get("settings").get("language")))
        .collect(Collectors.toList());
}
```

---

## 5. 测试验证流程

### 5.1 Persistence 层测试（H2）

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
})
class UserMapperTest {
    
    @Test
    void testSelectByUserId() {
        // Given: 插入测试数据
        User user = new User();
        user.setUserId("USER_001");
        userMapper.insert(user);
        
        // When: 查询
        User result = userMapper.selectByUserId("USER_001");
        
        // Then: 验证
        assertNotNull(result);
        assertEquals("USER_001", result.getUserId());
    }
}
```

### 5.2 Controller E2E 测试（PostgreSQL）

```java
@SpringBootTest
@Testcontainers
class UserControllerE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Test
    void testComplexQuery_E2E() {
        // 在真实 PostgreSQL 环境验证复杂查询
        // 包括 JSONB 操作、全文搜索等
    }
}
```

---

## 6. 兼容性检查清单

开发新功能时，SQL 必须通过以下检查：

- [ ] 在 Persistence 测试（H2）中运行通过
- [ ] 在 Controller E2E 测试（PostgreSQL）中运行通过
- [ ] 不使用数据库特有函数
- [ ] 不使用非标准语法
- [ ] 复杂 JSONB 操作在 E2E 测试中验证
- [ ] 使用标准 SQL 函数（UPPER, LOWER, SUBSTRING 等）
- [ ] 日期时间使用 CURRENT_TIMESTAMP
- [ ] 枚举使用 @Enumerated(EnumType.STRING)

---

## 7. 常见问题解决

### Q1: H2 不支持 PostgreSQL 的某个特性怎么办？

**A**: 优先在 Java 代码中实现，或在 Controller E2E 测试中跳过 Persistence 层测试。

```java
@Test
@EnabledIf("isPostgreSQL")  // 仅在 PostgreSQL 环境执行
void testPostgreSQLSpecificFeature() {
    // ...
}
```

### Q2: 如何快速发现兼容性问题？

**A**: 
1. 先在 Persistence 测试（H2）中运行 ✅
2. 再在 Controller E2E 测试（PostgreSQL）中验证 ✅
3. 如果 E2E 失败但 Persistence 成功，说明存在兼容性问题

### Q3: 生产环境遇到 SQL 错误怎么办？

**A**: 
1. 在 Controller E2E 测试中重现问题
2. 修改 SQL 为兼容写法
3. 确保 Persistence 和 E2E 测试都通过

---

**遵循此指南，确保 SQL 在测试和生产环境的一致性。**
