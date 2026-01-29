# Digital Human Module API Registry

> **Domain**: 数字人智能对话与引导  
> **Status**: Proposed  
> **Owner**: hangxiao  
> **Last Updated**: 2026-01-29

---

## Invokable APIs

### POST /api/v1/digital-human/sessions
- **描述**: 初始化对话会话，创建新的对话上下文
- **参数**: `CreateSessionRequest { userId: string, sceneType: string }`
  - `sceneType`: 场景类型 (profile_input/medical_input/glucose_record/diet_record/consultation/general)
- **返回**: `SessionResponse { sessionId, userId, sceneType, createdAt, expiresAt }`
- **异常**: 
  - `404` - 用户不存在
- **状态**: Proposed
- **关联场景**: SCN-004, SCN-008

---

### POST /api/v1/digital-human/sessions/{sessionId}/messages/stream
- **描述**: 🔥 核心优化：流式发送消息给数字人（SSE），支持打字机效果和语音流式播放
- **协议**: Server-Sent Events (SSE)
- **参数**: `SendMessageRequest { content: string, contentType: string }`
  - `contentType`: text/audio
- **返回**: SSE 事件流
  - `event: thinking` → `data: {"status": "thinking"}`
  - `event: text_chunk` → `data: {"text": "您好..."}`
  - `event: audio_chunk` → `data: {"audioUrl": "oss://xxx.mp3", "duration": 2.5}`
  - `event: tool_call` → `data: {"toolName": "save_blood_sugar", "params": {...}}`
  - `event: tool_result` → `data: {"result": "记录成功"}`
  - `event: done` → `data: {"messageId": "msg-xxx"}`
- **异常**: 
  - `404` - 会话不存在或已过期
  - `500` - AI 服务调用失败
- **状态**: Proposed
- **关联场景**: SCN-004

---

### POST /api/v1/digital-human/sessions/{sessionId}/messages
- **描述**: 降级接口：同步发送消息（非流式），用于不支持 SSE 的客户端
- **参数**: `SendMessageRequest { content: string, contentType: string }`
- **返回**: `MessageResponse { messageId, role: assistant, content, audioUrl?, toolCalls?, createdAt }`
- **异常**: 
  - `404` - 会话不存在
- **状态**: Proposed
- **关联场景**: SCN-004

---

### GET /api/v1/digital-human/sessions/{sessionId}/messages
- **描述**: 获取对话历史（分页）
- **参数**: `sessionId: string, page: int = 0, size: int = 20`
- **返回**: `List<MessageResponse>`
- **异常**: 
  - `404` - 会话不存在
- **状态**: Proposed
- **关联场景**: SCN-004, SCN-008

---

### GET /api/v1/digital-human/sessions/{sessionId}/guidance-status
- **描述**: 获取数字人引导流程状态（如个人信息录入进度）
- **参数**: `sessionId: string`
- **返回**: `GuidanceStatusResponse { sceneType, progress: {step, completed, total}, ... }`
- **异常**: 
  - `404` - 会话不存在
- **状态**: Proposed
- **关联场景**: SCN-003, SCN-004

---

### DELETE /api/v1/digital-human/sessions/{sessionId}
- **描述**: 结束对话会话
- **参数**: `sessionId: string`
- **返回**: `void`
- **异常**: 
  - `404` - 会话不存在
- **状态**: Proposed
- **关联场景**: SCN-004

---

## Tool APIs (Function Calling)

> 以下 APIs 由数字人 AI 通过 Function Calling 自动调用

### save_blood_sugar
- **描述**: 保存血糖记录（数字人工具）
- **参数**: `{ userId: string, value: decimal, type: string, time?: datetime }`
- **返回**: `{ success: boolean, recordId?: string, message: string }`
- **状态**: Proposed
- **调用方**: AI Agent (阿里百炼)

---

### save_diet
- **描述**: 保存饮食记录（数字人工具）
- **参数**: `{ userId: string, foods: array, totalCalories: decimal, ... }`
- **返回**: `{ success: boolean, recordId?: string, message: string }`
- **状态**: Proposed
- **调用方**: AI Agent

---

### save_exercise
- **描述**: 保存运动记录（数字人工具）
- **参数**: `{ userId: string, exerciseType: string, duration: int, ... }`
- **返回**: `{ success: boolean, recordId?: string, message: string }`
- **状态**: Proposed
- **调用方**: AI Agent

---

### query_latest_glucose
- **描述**: 查询最新血糖记录（数字人工具）
- **参数**: `{ userId: string }`
- **返回**: `{ value: decimal, type: string, time: datetime } | null`
- **状态**: Proposed
- **调用方**: AI Agent

---

## Constructable Classes

### Session (对话会话实体)
- **包路径**: `com.twelfth.digitalhuman.entity.Session`
- **关键字段**:
  - `sessionId: String` - 会话ID
  - `userId: String` - 用户ID
  - `sceneType: String` - 场景类型
  - `contextData: JSONObject` - 上下文数据（JSONB）
  - `expiresAt: LocalDateTime` - 过期时间（默认 30 分钟）

### Message (对话消息实体)
- **包路径**: `com.twelfth.digitalhuman.entity.Message`
- **关键字段**:
  - `messageId: String` - 消息ID
  - `sessionId: String` - 会话ID
  - `role: String` - 角色 (user/assistant/system)
  - `content: String` - 消息内容
  - `audioUrl: String` - 语音URL（TTS生成）
  - `toolCalls: JSONArray` - Function Call 记录

---

## 依赖关系

- **依赖**: 
  - User Module（用户身份）
  - 阿里百炼 AI 服务（对话、TTS、Function Calling）
  - Glucose/Diet/Exercise Modules（通过 Tool APIs 调用）
  
- **被依赖**: Consultation Module, 所有需要引导录入的场景
