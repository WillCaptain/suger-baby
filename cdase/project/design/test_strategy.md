# 测试策略与实施指南

> **Authority**: Convention §10  
> **Purpose**: 定义分层架构下的测试策略和最佳实践  
> **Last Updated**: 2026-01-29

---

## 1. 测试金字塔

```
           /\
          /  \         Controller 层测试（15%）
         /    \        - E2E 全量测试（不 mock）
        /------\       - PostgreSQL (TestContainers)
       /        \      
      /  Serv.  \     Service 层测试（25%）
     /   ice     \    - 业务流程测试
    /------------\   - Mock Persistence
   /              \  
  /    Entity      \ Entity 层测试（40%）⭐ 重点
 /    + Persist.   \- 核心业务逻辑
/------------------\- H2 内存数据库（快速）
       Persistence 层测试（20%）
```

**原则**：
- Entity 层测试覆盖率最高（⭐ 关键）
- Persistence 层使用 H2（快速）
- Controller E2E 层使用 PostgreSQL（真实）

---

## 2. Entity 层测试（⭐ 关键）

### 2.1 测试目标
- 验证所有业务方法逻辑
- 验证业务规则和约束
- 验证实体关联关系
- **不依赖持久化层**（纯内存对象测试）

### 2.2 示例：UserEntityTest.java

```java
package com.twelfth.user.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * User Entity 层测试
 * 重点测试业务方法和规则验证
 */
class UserEntityTest {
    
    private User guestUser;
    private User normalUser;
    
    @BeforeEach
    void setUp() {
        // 创建测试数据（不涉及持久化）
        guestUser = new User();
        guestUser.setUserId("GUEST_001");
        guestUser.setUserType(UserType.GUEST);
        guestUser.setAge(30);
        
        normalUser = new User();
        normalUser.setUserId("USER_001");
        normalUser.setUserType(UserType.NORMAL);
        normalUser.setEncryptedWechatOpenId("encrypted_openid_123");
    }
    
    @Test
    @DisplayName("测试访客身份判断")
    void testIsGuest() {
        // Given: 访客用户
        // When: 调用 isGuest()
        boolean result = guestUser.isGuest();
        
        // Then: 应返回 true
        assertTrue(result);
        assertFalse(normalUser.isGuest());
    }
    
    @Test
    @DisplayName("测试访客转正 - 成功场景")
    void testConvertToNormalUser_Success() {
        // Given: 访客用户
        String openId = "encrypted_openid_456";
        String unionId = "unionid_789";
        
        // When: 转正
        guestUser.convertToNormalUser(openId, unionId);
        
        // Then: 用户类型变为正式用户，OpenID 已设置
        assertEquals(UserType.NORMAL, guestUser.getUserType());
        assertEquals(openId, guestUser.getEncryptedWechatOpenId());
        assertEquals(unionId, guestUser.getWechatUnionid());
        assertFalse(guestUser.isGuest());
    }
    
    @Test
    @DisplayName("测试访客转正 - 失败场景：正式用户不能转正")
    void testConvertToNormalUser_Fail_AlreadyNormal() {
        // Given: 正式用户
        // When & Then: 转正应抛出异常
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> normalUser.convertToNormalUser("openid", "unionid")
        );
        
        assertEquals("只有访客可以转正", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试隐私协议同意")
    void testAgreePrivacyPolicy() {
        // Given: 用户未同意隐私协议
        assertNull(guestUser.getPrivacyAgreedAt());
        
        // When: 同意协议
        String version = "v1.0";
        guestUser.agreePrivacyPolicy(version);
        
        // Then: 协议版本和时间已记录
        assertEquals(version, guestUser.getPrivacyAgreementVersion());
        assertNotNull(guestUser.getPrivacyAgreedAt());
        assertTrue(guestUser.hasAgreedPrivacyPolicy());
    }
    
    @Test
    @DisplayName("测试添加血糖记录（实体关联）")
    void testAddGlucoseRecord() {
        // Given: 用户和血糖记录
        GlucoseRecord record = new GlucoseRecord();
        record.setRecordId("RECORD_001");
        record.setGlucoseValue(new BigDecimal("6.5"));
        
        // When: 添加记录
        guestUser.addGlucoseRecord(record);
        
        // Then: 双向关联已建立
        assertEquals(1, guestUser.getGlucoseRecords().size());
        assertEquals(record, guestUser.getGlucoseRecords().get(0));
        assertEquals(guestUser, record.getUser());  // 验证双向关联
    }
    
    @Test
    @DisplayName("测试年龄计算（基于出生日期）")
    void testGetCalculatedAge() {
        // Given: 用户有出生日期
        guestUser.setDateOfBirth(LocalDate.of(1990, 5, 15));
        
        // When: 获取计算年龄
        Integer age = guestUser.getCalculatedAge();
        
        // Then: 年龄正确计算（2026 - 1990 = 36）
        assertEquals(36, age);
    }
}
```

### 2.3 Entity 测试覆盖清单

对于每个 Entity，必须测试：
- [ ] 所有业务方法（如：`isGuest()`, `convertToNormalUser()`）
- [ ] 业务规则验证（如：转正前置条件检查）
- [ ] 实体关联操作（如：`addGlucoseRecord()`）
- [ ] 计算字段（如：`getCalculatedAge()`）
- [ ] 异常场景（如：非法状态转换）

---

## 3. Persistence 层测试

### 3.1 测试目标
- 验证 SQL 语句正确性
- 验证数据查询和更新逻辑
- **使用 H2 内存数据库（快速测试）**
- ⚠️ SQL 需兼容 H2 和 PostgreSQL

### 3.2 示例：UserMapperTest.java

```java
package com.twelfth.user.mapper;

import com.twelfth.user.entity.User;
import com.twelfth.user.entity.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper Persistence 层测试
 * 使用 H2 内存数据库（PostgreSQL 模式）快速测试
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    @DisplayName("测试根据用户ID查询用户")
    void testSelectByUserId() {
        // Given: 数据库中存在用户 GUEST_001
        String userId = "GUEST_001";
        
        // When: 查询用户
        User user = userMapper.selectByUserId(userId);
        
        // Then: 用户存在且数据正确
        assertNotNull(user);
        assertEquals(userId, user.getUserId());
        assertEquals(UserType.GUEST, user.getUserType());
    }
    
    @Test
    @DisplayName("测试根据 OpenID 查询用户")
    void testSelectByWechatOpenId() {
        // Given: 数据库中存在 OpenID 对应的用户
        String openIdHash = "hash_123";
        
        // When: 通过 OpenID 哈希查询
        User user = userMapper.selectByWechatOpenIdHash(openIdHash);
        
        // Then: 用户存在
        assertNotNull(user);
        assertEquals(openIdHash, user.getWechatOpenIdHash());
    }
    
    @Test
    @DisplayName("测试插入访客用户")
    void testInsert() {
        // Given: 新的访客用户
        User newUser = new User();
        newUser.setUserId("GUEST_999");
        newUser.setUserType(UserType.GUEST);
        newUser.setAge(25);
        
        // When: 插入数据库
        int rows = userMapper.insert(newUser);
        
        // Then: 插入成功
        assertEquals(1, rows);
        
        // 验证可查询到
        User savedUser = userMapper.selectByUserId("GUEST_999");
        assertNotNull(savedUser);
        assertEquals(25, savedUser.getAge());
    }
    
    @Test
    @DisplayName("测试更新用户信息")
    void testUpdate() {
        // Given: 数据库中存在用户
        User user = userMapper.selectByUserId("GUEST_001");
        user.setAge(35);
        user.setUserType(UserType.NORMAL);
        
        // When: 更新用户
        int rows = userMapper.updateByUserId(user);
        
        // Then: 更新成功
        assertEquals(1, rows);
        
        // 验证更新生效
        User updatedUser = userMapper.selectByUserId("GUEST_001");
        assertEquals(35, updatedUser.getAge());
        assertEquals(UserType.NORMAL, updatedUser.getUserType());
    }
}
```

---

## 4. Service 层测试

### 4.1 测试目标
- 验证业务流程编排
- 验证事务边界
- Mock Persistence 层依赖

### 4.2 示例：UserServiceTest.java

```java
package com.twelfth.user.service.impl;

import com.twelfth.user.entity.User;
import com.twelfth.user.entity.UserType;
import com.twelfth.user.mapper.UserMapper;
import com.twelfth.user.dto.ConvertUserRequest;
import com.twelfth.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserService Service 层测试
 * Mock Persistence 层，测试业务编排逻辑
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private EncryptionService encryptionService;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    @DisplayName("测试创建访客用户")
    void testCreateGuestUser() {
        // Given: Mock mapper 行为
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // When: 创建访客
        GuestTokenResponse response = userService.createGuestUser();
        
        // Then: 返回令牌和用户ID
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertNotNull(response.getToken());
        assertTrue(response.getUserId().startsWith("GUEST_"));
        
        // 验证 mapper 被调用
        verify(userMapper, times(1)).insert(any(User.class));
    }
    
    @Test
    @DisplayName("测试访客转正 - 新用户场景")
    void testConvertGuestToUser_NewUser() {
        // Given: 访客用户存在，OpenID 不存在（新用户）
        String guestUserId = "GUEST_001";
        User guestUser = new User();
        guestUser.setUserId(guestUserId);
        guestUser.setUserType(UserType.GUEST);
        
        when(userMapper.selectByUserId(guestUserId)).thenReturn(guestUser);
        when(userMapper.selectByWechatOpenIdHash(anyString())).thenReturn(null);  // 新用户
        when(userMapper.updateByUserId(any(User.class))).thenReturn(1);
        
        ConvertUserRequest request = new ConvertUserRequest();
        request.setWechatOpenId("openid_123");
        
        // When: 转正
        UserResponse response = userService.convertGuestToUser(guestUserId, request);
        
        // Then: 访客账号直接升级
        assertNotNull(response);
        assertEquals(guestUserId, response.getUserId());
        
        // 验证调用流程
        verify(userMapper).selectByUserId(guestUserId);
        verify(userMapper).selectByWechatOpenIdHash(anyString());
        verify(userMapper).updateByUserId(any(User.class));
    }
    
    @Test
    @DisplayName("测试访客转正 - 老用户数据迁移场景")
    void testConvertGuestToUser_ExistingUser_DataMigration() {
        // Given: 访客用户存在，OpenID 对应老用户（需迁移）
        String guestUserId = "GUEST_001";
        User guestUser = new User();
        guestUser.setUserId(guestUserId);
        guestUser.setUserType(UserType.GUEST);
        
        User existingUser = new User();
        existingUser.setUserId("USER_001");
        existingUser.setUserType(UserType.NORMAL);
        
        when(userMapper.selectByUserId(guestUserId)).thenReturn(guestUser);
        when(userMapper.selectByWechatOpenIdHash(anyString())).thenReturn(existingUser);  // 老用户
        when(glucoseRecordMapper.updateUserIdByUserId(guestUserId, existingUser.getUserId())).thenReturn(5);
        when(userMapper.updateByUserId(any(User.class))).thenReturn(1);
        
        ConvertUserRequest request = new ConvertUserRequest();
        request.setWechatOpenId("openid_123");
        
        // When: 转正
        UserResponse response = userService.convertGuestToUser(guestUserId, request);
        
        // Then: 数据迁移到老账号
        assertNotNull(response);
        assertEquals("USER_001", response.getUserId());
        
        // 验证数据迁移逻辑被调用
        verify(glucoseRecordMapper).updateUserIdByUserId(guestUserId, existingUser.getUserId());
        verify(userMapper).updateByUserId(argThat(user -> user.getIsDeleted()));  // 访客账号软删除
    }
}
```

---

## 5. Controller 层测试（E2E 端到端全量测试）

### 5.1 测试目标
- **端到端测试，验证完整业务流程**
- **不 mock 任何层**，使用真实的 Service、Persistence 和数据库
- 验证 HTTP 协议（请求/响应）
- 验证数据持久化和业务逻辑
- 使用 TestContainers 启动真实 PostgreSQL

### 5.2 示例：UserControllerE2ETest.java

```java
package com.twelfth.user.controller;

import com.twelfth.user.entity.User;
import com.twelfth.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserController E2E 端到端测试
 * 不 mock 任何层，使用真实 PostgreSQL 数据库
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional  // 每个测试后回滚
class UserControllerE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserMapper userMapper;  // 用于验证数据库状态
    
    @BeforeEach
    void setUp() {
        // 清理测试数据（如需要）
    }
    
    @Test
    @DisplayName("E2E 测试：访客注册接口 - 完整流程")
    void testRegisterGuest_E2E() throws Exception {
        // When: 调用访客注册接口（无需 mock）
        String response = mockMvc.perform(post("/api/v1/users/guest/register")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.expiresAt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        // Then: 验证数据库中确实创建了访客用户
        // 解析返回的 userId
        String userId = extractUserId(response);
        User savedUser = userMapper.selectByUserId(userId);
        
        assertNotNull(savedUser);
        assertTrue(savedUser.isGuest());
        assertEquals(userId, savedUser.getUserId());
    }
    
    @Test
    @DisplayName("E2E 测试：访客转正接口 - 新用户场景")
    void testConvertGuestToUser_NewUser_E2E() throws Exception {
        // Given: 先创建一个访客用户（真实创建，不 mock）
        String guestResponse = mockMvc.perform(post("/api/v1/users/guest/register"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String guestUserId = extractUserId(guestResponse);
        String guestToken = extractToken(guestResponse);
        
        // When: 访客转正（新用户场景）
        mockMvc.perform(post("/api/v1/users/guest/convert")
                .header("Authorization", "Bearer " + guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wechatOpenId\":\"new_openid_123\",\"wechatUnionId\":\"unionid_456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(guestUserId))
            .andExpect(jsonPath("$.userType").value("NORMAL"));
        
        // Then: 验证数据库中用户已转正
        User convertedUser = userMapper.selectByUserId(guestUserId);
        assertNotNull(convertedUser);
        assertFalse(convertedUser.isGuest());
        assertTrue(convertedUser.isNormalUser());
        assertNotNull(convertedUser.getEncryptedWechatOpenId());
    }
    
    @Test
    @DisplayName("E2E 测试：访客转正接口 - 老用户数据迁移场景")
    void testConvertGuestToUser_ExistingUser_DataMigration_E2E() throws Exception {
        // Given: 先创建一个老的正式用户
        User existingUser = createExistingUser("old_openid_789");
        
        // 再创建一个访客用户，并记录一些数据（血糖、饮食等）
        String guestResponse = mockMvc.perform(post("/api/v1/users/guest/register"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String guestUserId = extractUserId(guestResponse);
        String guestToken = extractToken(guestResponse);
        
        // 访客记录血糖数据
        mockMvc.perform(post("/api/v1/glucose/records")
                .header("Authorization", "Bearer " + guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"glucoseValue\":6.5,\"measurementType\":\"after_meal\"}"))
            .andExpect(status().isOk());
        
        // When: 访客使用老用户的 OpenID 转正（数据迁移场景）
        mockMvc.perform(post("/api/v1/users/guest/convert")
                .header("Authorization", "Bearer " + guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wechatOpenId\":\"old_openid_789\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(existingUser.getUserId()));  // 返回老用户ID
        
        // Then: 验证数据迁移
        // 1. 访客账号已软删除
        User deletedGuest = userMapper.selectByUserId(guestUserId);
        assertTrue(deletedGuest.getIsDeleted());
        
        // 2. 血糖数据已迁移到老账号
        List<GlucoseRecord> records = glucoseRecordMapper.selectByUserId(existingUser.getUserId());
        assertEquals(1, records.size());
        assertEquals(new BigDecimal("6.5"), records.get(0).getGlucoseValue());
    }
    
    @Test
    @DisplayName("E2E 测试：参数校验")
    void testValidation_E2E() throws Exception {
        // When & Then: 缺少必填参数
        mockMvc.perform(post("/api/v1/users/guest/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))  // 空请求体
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
    
    // Helper methods
    private String extractUserId(String jsonResponse) {
        // 解析 JSON 返回 userId
        // ...
    }
    
    private String extractToken(String jsonResponse) {
        // 解析 JSON 返回 token
        // ...
    }
    
    private User createExistingUser(String openId) {
        User user = new User();
        user.setUserId("USER_OLD_001");
        user.setUserType(UserType.NORMAL);
        user.setEncryptedWechatOpenId(openId);
        userMapper.insert(user);
        return user;
    }
}
```

### 5.3 E2E 测试关键点

**优势**：
- ✅ 测试真实的端到端业务流程
- ✅ 验证各层之间的集成
- ✅ 发现集成问题和边界条件
- ✅ 测试数据持久化和事务

**注意事项**：
- ⚠️ 测试较慢（启动完整应用 + PostgreSQL 容器）
- ⚠️ 需要合理控制测试数量（避免过多 E2E 测试）
- ⚠️ 使用 `@Transactional` 确保测试数据隔离
- ⚠️ TestContainers 容器在所有测试间共享（`@Container static`）

---

## 6. 测试覆盖率要求

| 层次 | 覆盖率目标 | 测试类型 | 数据库 | 重点 |
|------|----------|---------|--------|------|
| **Entity 层** | **≥ 90%** | 单元测试 | 无（纯内存） | ⭐ 关键测试，所有业务方法必须覆盖 |
| Persistence 层 | ≥ 80% | 集成测试 | **H2 内存数据库** | 快速测试，CRUD 操作必须覆盖 |
| Service 层 | ≥ 85% | 单元测试 | Mock | 核心业务流程必须覆盖 |
| **Controller 层** | **≥ 80%** | **E2E 端到端** | **PostgreSQL (TestContainers)** | ⭐ 完整场景，SQL 兼容性验证 |

---

## 7. 测试命名规范

### 7.1 测试类命名
- 格式：`{ClassName}Test.java`
- 示例：`UserEntityTest.java`, `UserMapperTest.java`

### 7.2 测试方法命名
- 格式：`test{MethodName}_{Scenario}`
- 使用 `@DisplayName` 注解提供中文描述
- 示例：
  ```java
  @Test
  @DisplayName("测试访客转正 - 成功场景")
  void testConvertToNormalUser_Success() { ... }
  ```

---

## 8. 持续集成（CI）

测试在 CI 流程中的执行顺序：

```
1. Entity 层测试（最快，纯内存）
   ↓
2. Service 层测试（Mock Persistence）
   ↓
3. Persistence 层测试（TestContainers PostgreSQL）
   ↓
4. Controller E2E 测试（完整应用 + PostgreSQL）
   ↓
5. 生成覆盖率报告
   ↓
6. 覆盖率检查（不达标则失败）
```

**CI 优化建议**：
- Entity 和 Service 层测试并行执行（无依赖）
- TestContainers 容器在测试套件间复用
- Controller E2E 测试最后执行（最慢）

---

**遵循此测试策略，确保代码质量和可维护性。**
