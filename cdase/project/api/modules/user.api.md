# User Module API Registry

> **Domain**: 用户身份与信息管理  
> **Status**: Proposed  
> **Owner**: hangxiao  
> **Last Updated**: 2026-01-29

---

## Invokable APIs

### POST /api/v1/users/guest/register
- **描述**: 创建访客用户，生成临时访客身份用于 showcase 演示
- **参数**: 无
- **返回**: `GuestTokenResponse { userId: string, token: string, expiresAt: datetime }`
- **异常**: 
  - `500` - 系统错误
- **状态**: Proposed
- **关联场景**: SCN-001

---

### POST /api/v1/users/guest/convert
- **描述**: 访客转正为正式用户，支持新用户升级和老用户数据迁移
- **参数**: `ConvertUserRequest { guestUserId: string, phone: string, smsCode: string }`
- **返回**: `UserResponse { userId: string, userType: string, phone: string (脱敏), ... }`
- **异常**: 
  - `400` - 访客用户不存在或已转正、验证码错误
  - `500` - 数据迁移失败（事务回滚）
- **状态**: Proposed (P1)
- **关联场景**: SCN-001

---

### GET /api/v1/users/{userId}
- **描述**: 获取用户基本信息（自动解密敏感字段）
- **参数**: `userId: string`
- **返回**: `UserResponse { userId, name, gender, age, phone (脱敏), ... }`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-003

---

### PUT /api/v1/users/{userId}/profile
- **描述**: 更新用户基本信息（姓名、性别、年龄、联系方式等）
- **参数**: `UpdateProfileRequest { name?, gender?, age?, phone?, ... }`
- **返回**: `UserResponse`
- **异常**: 
  - `403` - 访客不可修改（仅正式用户）
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-003

---

### PUT /api/v1/users/{userId}/medical-history
- **描述**: 更新用户病例信息（患病时间、血压、病史、用药情况等）
- **参数**: `UpdateMedicalHistoryRequest { diabetesType?, onsetDate?, bloodPressure?, medications?, ... }`
- **返回**: `MedicalHistoryResponse`
- **异常**: 
  - `403` - 访客不可修改
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-003

---

### GET /api/v1/users/{userId}/medical-history
- **描述**: 获取用户病例信息（自动解密敏感字段）
- **参数**: `userId: string`
- **返回**: `MedicalHistoryResponse { diabetesType, onsetDate, bloodPressure, medications, ... }`
- **异常**: 
  - `404` - 用户或病例不存在
- **状态**: Proposed
- **关联场景**: SCN-003, SCN-008

---

### POST /api/v1/users/{userId}/privacy-agreement
- **描述**: 记录用户同意隐私协议
- **参数**: `PrivacyAgreementRequest { agreementVersion: string }`
- **返回**: `void`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-001

---

## Constructable Classes

### User (用户实体)
- **包路径**: `com.twelfth.user.entity.User`
- **描述**: 用户基本信息实体（严格面向对象设计）
- **关键字段**:
  - `userId: String` - 用户唯一ID (GUEST_* 或 USER_*)
  - `userType: String` - 用户类型 (guest/normal)
  - `encryptedPhone: String` - 加密的手机号（仅正式用户有）
  - `phoneHash: String` - 手机号哈希（用于查询，仅正式用户有）
  - `encryptedName: String` - 加密的姓名（可选）
  - `gender: String` - 性别 (male/female)
  - `age: Integer` - 年龄
  - `isDeleted: Boolean` - 软删除标记

### MedicalHistory (病例实体)
- **包路径**: `com.twelfth.user.entity.MedicalHistory`
- **描述**: 用户医疗病史信息（严格面向对象设计）
- **关键字段**:
  - `medicalHistoryId: String` - 病例ID
  - `userId: String` - 关联用户ID
  - `diabetesType: String` - 糖尿病类型 (type1/type2)
  - `onsetDate: LocalDate` - 患病日期
  - `encryptedMedications: String` - 加密的用药信息
  - `bloodPressure: String` - 血压（格式：120/80）

---

## 依赖关系

- **依赖外部服务**: 
  - 短信服务商（发送验证码，P1 阶段）
  - 安全模块（加密解密服务）

- **被依赖**:
  - 所有业务模块都依赖用户模块进行身份验证
