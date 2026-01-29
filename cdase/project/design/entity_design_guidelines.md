# Entity 层面向对象设计规范

> **Authority**: Convention §7  
> **Purpose**: 定义 Entity 层的面向对象设计标准  
> **Last Updated**: 2026-01-29

---

## 1. 基础原则

### 1.1 封装性（Encapsulation）
- 所有字段必须使用 `private` 修饰符
- 提供必要的 Getter/Setter 方法
- 敏感字段（如密码、令牌）只提供 Setter，不提供 Getter
- 计算字段（如年龄）只提供 Getter，不提供 Setter

### 1.2 继承（Inheritance）
- 使用 `BaseEntity` 抽取通用字段
- 合理使用抽象类表达"is-a"关系
- 避免过深的继承层次（≤ 3 层）

### 1.3 多态（Polymorphism）
- 使用接口定义行为契约
- 通过继承实现不同的业务逻辑

---

## 2. 标准 Entity 结构

### 2.1 BaseEntity（基础实体）

```java
package com.twelfth.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 基础实体类，包含所有 Entity 的通用字段
 */
@MappedSuperclass
@Data
public abstract class BaseEntity {
    
    /**
     * 主键ID（UUID）
     */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 软删除标记
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    /**
     * 创建前自动设置时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
    
    /**
     * 更新前自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 逻辑删除
     */
    public void softDelete() {
        this.isDeleted = true;
    }
}
```

---

### 2.2 业务 Entity 示例：User（用户实体）

```java
package com.twelfth.user.entity;

import com.twelfth.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;

/**
 * 用户实体
 * 遵循面向对象设计原则：封装、继承、业务方法
 */
@Entity
@Table(name = "tbl_user", indexes = {
    @Index(name = "idx_wechat_openid", columnList = "wechat_openid_hash"),
    @Index(name = "idx_phone_hash", columnList = "phone_hash")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    // ============ 基本信息字段 ============
    
    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId;  // 业务用户ID（可读）
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType = UserType.GUEST;  // 用户类型枚举
    
    @Column(name = "encrypted_name", length = 200)
    private String encryptedName;  // 加密的姓名
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;  // 性别枚举
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    // ============ 加密与哈希字段 ============
    
    @Column(name = "encrypted_phone", length = 200)
    private String encryptedPhone;  // 加密的手机号
    
    @Column(name = "phone_hash", length = 64)
    private String phoneHash;  // 手机号哈希（用于查询）
    
    @Column(name = "encrypted_wechat_openid", length = 200)
    private String encryptedWechatOpenId;
    
    @Column(name = "wechat_openid_hash", length = 64)
    private String wechatOpenIdHash;
    
    @Column(name = "wechat_unionid", length = 100)
    private String wechatUnionid;
    
    // ============ 隐私协议字段 ============
    
    @Column(name = "privacy_agreement_version", length = 20)
    private String privacyAgreementVersion;
    
    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;
    
    // ============ 关联关系（面向对象） ============
    
    /**
     * 一对一关联：用户 → 病例信息
     * 使用对象引用而非仅存储 ID
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MedicalHistory medicalHistory;
    
    /**
     * 一对多关联：用户 → 血糖记录
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GlucoseRecord> glucoseRecords = new ArrayList<>();
    
    // ============ 业务方法（面向对象核心） ============
    
    /**
     * 判断是否为访客
     */
    public boolean isGuest() {
        return this.userType == UserType.GUEST;
    }
    
    /**
     * 判断是否为正式用户
     */
    public boolean isNormalUser() {
        return this.userType == UserType.NORMAL;
    }
    
    /**
     * 访客转正
     * 业务逻辑封装在 Entity 内部
     */
    public void convertToNormalUser(String wechatOpenId, String wechatUnionid) {
        if (!this.isGuest()) {
            throw new IllegalStateException("只有访客可以转正");
        }
        this.userType = UserType.NORMAL;
        this.encryptedWechatOpenId = wechatOpenId;  // 假设已加密
        this.wechatUnionid = wechatUnionid;
    }
    
    /**
     * 同意隐私协议
     */
    public void agreePrivacyPolicy(String version) {
        this.privacyAgreementVersion = version;
        this.privacyAgreedAt = LocalDateTime.now();
    }
    
    /**
     * 判断隐私协议是否已同意
     */
    public boolean hasAgreedPrivacyPolicy() {
        return this.privacyAgreedAt != null;
    }
    
    /**
     * 计算年龄（基于出生日期）
     * 计算字段，无需持久化
     */
    @Transient
    public Integer getCalculatedAge() {
        if (this.dateOfBirth == null) {
            return this.age;
        }
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }
    
    /**
     * 添加血糖记录（业务方法）
     */
    public void addGlucoseRecord(GlucoseRecord record) {
        this.glucoseRecords.add(record);
        record.setUser(this);  // 双向关联
    }
}
```

---

### 2.3 枚举定义

```java
package com.twelfth.user.entity;

/**
 * 用户类型枚举
 */
public enum UserType {
    GUEST("访客"),
    NORMAL("正式用户"),
    VIP("VIP会员");
    
    private final String description;
    
    UserType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * 性别枚举
 */
public enum Gender {
    MALE("男"),
    FEMALE("女"),
    OTHER("其他");
    
    private final String description;
    
    Gender(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

---

## 3. 设计原则总结

### ✅ **DO（推荐做法）**

1. **继承 BaseEntity**：所有 Entity 都应继承 `BaseEntity`
2. **使用枚举**：状态字段使用 `@Enumerated` 枚举类型，避免字符串魔法值
3. **对象关联**：使用 JPA 关联注解（@OneToOne, @OneToMany, @ManyToOne）表达实体关系
4. **封装业务逻辑**：将简单的业务判断和操作封装在 Entity 内（如：`isGuest()`, `convertToNormalUser()`）
5. **双向关联维护**：关联关系的双向绑定在业务方法中维护（如：`addGlucoseRecord()`）
6. **使用 Lombok**：减少样板代码（@Data, @EqualsAndHashCode, @ToString）

### ❌ **DON'T（避免做法）**

1. **贫血模型**：Entity 不应只是数据容器，需要包含必要的业务方法
2. **过度封装**：不要为每个字段都写自定义 Getter/Setter（Lombok 已处理）
3. **字符串魔法值**：避免直接使用 `"guest"`, `"normal"` 等字符串，使用枚举
4. **仅存储 ID**：关联关系应使用对象引用，而非仅存储 `userId` 等 ID 字段
5. **过深继承**：避免超过 3 层的继承关系

---

## 4. 关联关系设计

### 4.1 一对一（@OneToOne）

```java
// User.java
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private MedicalHistory medicalHistory;

// MedicalHistory.java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", referencedColumnName = "user_id")
private User user;
```

### 4.2 一对多（@OneToMany）

```java
// User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<GlucoseRecord> glucoseRecords = new ArrayList<>();

// GlucoseRecord.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", referencedColumnName = "user_id")
private User user;
```

---

## 5. 验证规范

所有 Entity 必须通过以下检查：

- [ ] 继承 BaseEntity
- [ ] 所有字段使用 `private` 修饰符
- [ ] 使用枚举代替字符串状态值
- [ ] 关联关系使用对象引用
- [ ] 至少包含 1 个业务方法
- [ ] 使用 JPA 注解（@Entity, @Table, @Column, @Id）
- [ ] 索引字段添加 `@Index` 注解

---

**遵循此规范，确保 Entity 层设计的一致性和可维护性。**
