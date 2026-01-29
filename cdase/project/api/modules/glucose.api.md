# Glucose Management Module API Registry

> **Domain**: 血糖数据记录与统计  
> **Status**: Proposed  
> **Owner**: hangxiao  
> **Last Updated**: 2026-01-29

---

## Invokable APIs

### POST /api/v1/glucose/records
- **描述**: 创建血糖记录，支持手动录入和数字人引导录入
- **参数**: `CreateGlucoseRecordRequest { userId: string, glucoseValue: decimal, measurementType: string, measurementTime?: datetime, notes?: string, sessionId?: string }`
- **返回**: `GlucoseRecordResponse { recordId, userId, glucoseValue, measurementType, measurementTime, ... }`
- **异常**: 
  - `400` - 血糖值超出有效范围 (1.0 - 33.3 mmol/L)
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-002, SCN-004

---

### POST /api/v1/glucose/records/batch
- **描述**: 批量创建血糖记录，支持从设备导入
- **参数**: `List<CreateGlucoseRecordRequest>`
- **返回**: `BatchCreateResponse { successCount, failedCount, failedRecords[] }`
- **异常**: 
  - `400` - 部分记录验证失败
- **状态**: Proposed
- **关联场景**: SCN-002

---

### GET /api/v1/glucose/records
- **描述**: 获取用户血糖记录列表（分页）
- **参数**: `userId: string, startDate?: date, endDate?: date, page: int = 0, size: int = 20`
- **返回**: `PageResponse<GlucoseRecordResponse> { content[], totalElements, totalPages, ... }`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-002, SCN-007, SCN-008

---

### GET /api/v1/glucose/records/latest
- **描述**: 获取最新血糖记录
- **参数**: `userId: string`
- **返回**: `GlucoseRecordResponse | null`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-007

---

### GET /api/v1/glucose/statistics
- **描述**: 获取血糖统计信息（平均值、最高值、最低值、达标率等）
- **参数**: `userId: string, startDate?: date, endDate?: date`
- **返回**: `GlucoseStatisticsResponse { averageValue, maxValue, minValue, targetAchievementRate, recordCount, ... }`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-002

---

### DELETE /api/v1/glucose/records/{recordId}
- **描述**: 删除血糖记录
- **参数**: `recordId: string`
- **返回**: `void`
- **异常**: 
  - `404` - 记录不存在
  - `403` - 无权删除（不是记录所有者）
- **状态**: Proposed
- **关联场景**: SCN-002

---

## Constructable Classes

### GlucoseRecord (血糖记录实体)
- **包路径**: `com.twelfth.glucose.entity.GlucoseRecord`
- **描述**: 血糖测量记录（严格面向对象设计）
- **关键字段**:
  - `recordId: String` - 记录唯一ID
  - `userId: String` - 用户ID
  - `glucoseValue: BigDecimal` - 血糖值 (mmol/L)
  - `measurementType: String` - 测量类型 (fasting/before_meal/after_meal/before_sleep/random)
  - `measurementTime: LocalDateTime` - 测量时间
  - `notes: String` - 备注
  - `sessionId: String` - 关联数字人会话ID（数字人录入时）
  - `createdAt: LocalDateTime` - 创建时间

---

## 依赖关系

- **依赖**: User Module（用户身份验证）
- **被依赖**: Dashboard Module, Digital Human Module, Consultation Module
