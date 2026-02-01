# Glucose Management Module API Registry

> **Domain**: 血糖数据记录与统计  
> **Status**: Design In Progress  
> **Owner**: hangxiao  
> **Last Updated**: 2026-01-31
> **Related Features**: FTR-003, FTR-004

---

## Invokable APIs

### POST /api/v1/glucose/records
- **描述**: 创建血糖记录，支持手动录入和数字人引导录入
- **认证**: Bearer Token 或 X-Guest-Id Header
- **请求体**: 
  ```json
  {
    "value": 6.5,
    "measureType": "after_meal",
    "measuredAt": 1706543210000,
    "note": "今天吃了甜食"
  }
  ```
- **返回**: 
  ```json
  {
    "id": "uuid",
    "userId": "GUEST_xxx 或正式用户ID",
    "value": 6.5,
    "measureType": "after_meal",
    "measuredAt": 1706543210000,
    "note": "今天吃了甜食",
    "createdAt": 1706543215000,
    "updatedAt": 1706543215000
  }
  ```
- **异常**: 
  - `400 BadRequest` - 血糖值超出有效范围 (1.0 - 33.3 mmol/L)
  - `401 Unauthorized` - 未授权
  - `500 InternalServerError` - 系统错误
- **状态**: Design In Progress
- **关联**: FTR-003 (FUN-005), SCN-002, SCN-004

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
- **认证**: Bearer Token 或 X-Guest-Id Header
- **查询参数**: 
  - `page` (int): 页码，默认 1
  - `pageSize` (int): 每页条数，默认 20
  - `startDate` (date): 开始日期（可选）
  - `endDate` (date): 结束日期（可选）
- **返回**: 
  ```json
  {
    "records": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
  ```
- **异常**: 
  - `401 Unauthorized` - 未授权
- **状态**: Design In Progress
- **关联**: FTR-003 (FUN-006, FUN-007), SCN-002, SCN-007, SCN-008

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
- **认证**: Bearer Token 或 X-Guest-Id Header
- **查询参数**: 
  - `period` (enum): 统计周期（today/last7days/last30days），默认 today
  - `targetValue` (float): 达标目标值（mmol/L），默认 7.0
- **返回**: 
  ```json
  {
    "period": "last7days",
    "recordCount": 21,
    "averageValue": 6.8,
    "maxValue": 9.2,
    "minValue": 4.5,
    "targetRate": 85.7,
    "targetValue": 7.0
  }
  ```
- **异常**: 
  - `401 Unauthorized` - 未授权
  - `500 InternalServerError` - 系统错误
- **状态**: Design In Progress
- **关联**: FTR-004 (FUN-010), SCN-002

---

### DELETE /api/v1/glucose/records/{id}
- **描述**: 删除血糖记录
- **认证**: Bearer Token 或 X-Guest-Id Header
- **路径参数**: `id` (UUID): 记录ID
- **返回**: 
  ```json
  {
    "success": true
  }
  ```
- **异常**: 
  - `404 NotFound` - 记录不存在
  - `403 Forbidden` - 无权删除他人记录
  - `401 Unauthorized` - 未授权
- **状态**: Design In Progress
- **关联**: FTR-003 (FUN-008), SCN-002

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
