> NOTE:
> The Stage Ownership table is the single source of truth
> for execution state, ownership, and task discovery.
> AI MUST NOT infer stage state from any other fields.
> all information should be short, concise, accurate

# Feature: FTR-001 访客ID生成与管理

## 0. Metadata
- ID: FTR-001
- SCN ID: SCN-001
- Steward: hangxiao
- Group/Module: User/GuestManagement
- Priority: P0
- Version: v1.0
- Last Updated: 2026-01-31 14:30
- Resolution Status: ✅ Accepted and Complete
- Change Log: 
  - v1.0 - ✅ Feature 完成并验收：所有 FAC 通过，真机测试通过
  - v0.4 - 完成 Development 阶段：所有单元测试和集成测试通过
  - v0.3 - 完成 Design 阶段：创建 Function 文档、序列图、类图
  - v0.2 - 简化功能，移除令牌管理，仅保留访客ID生成
- Depends On: 
  - 无外部依赖（这是系统的入口功能）

-----------------------------------------------------------------------------------------
## 1. Summary (One Paragraph)

允许新用户无需注册即可快速体验"糖小稳"应用的核心功能。系统自动生成临时访客身份（Guest ID）并存储在本地，访客可立即使用血糖记录、饮食记录、运动记录和数字人对话等功能。访客ID无过期时间，长期有效直到用户清除缓存。这个功能是 Showcase 演示的关键，降低用户初次使用门槛。

## 2. API / SPI Contract (Frozen Boundary)

### API (Provided)
- **REST** `POST /api/v1/users/guest/register`
  - **Description**: 创建访客用户，生成临时访客身份ID
  - **Params**: 无（自动生成）
  - **Returns**: `GuestIdResponse { guestId: string }`
  - **Throws**: 
    - `500 InternalServerError` - 系统错误，访客创建失败
  - **Note**: 返回的 guestId 由前端存储到本地缓存

-----------------------------------------------------------------------------------------

## 3. Stage Ownership and Execution State

> This table is the single source of truth for Feature execution state.

| Stage        | Status       | Owner        | Assigned At        | Last Updated       | Completed At       | Blocked Reason |
|--------------|--------------|--------------|--------------------|--------------------|--------------------|----------------|
| Requirement  | Done         | hangxiao     | 2026-01-29 19:45   | 2026-01-29 19:50   | 2026-01-29 19:50   | -              |
| Design       | Done         | hangxiao     | 2026-01-29 21:40   | 2026-01-29 21:50   | 2026-01-29 21:50   | -              |
| Development  | Done         | hangxiao     | 2026-01-30 10:00   | 2026-01-31 14:00   | 2026-01-31 14:00   | -              |
| Test         | Done         | hangxiao     | 2026-01-31 14:00   | 2026-01-31 14:30   | 2026-01-31 14:30   | -              |
| Acceptance   | Done         | hangxiao     | 2026-01-31 14:30   | 2026-01-31 14:30   | 2026-01-31 14:30   | -              |

## 4. User Journey / Flow (Text)

1. **用户打开 Android 应用**：用户首次打开"糖小稳"应用
2. **系统检测新用户**：检测到本地无访客ID [FUN-001]（共享模块逻辑）
3. **自动生成访客ID**：客户端生成唯一访客 ID（格式：`GUEST_<timestamp>_<random>`）[FUN-002]（共享模块逻辑）
4. **保存访客ID到本地**：通过平台特定实现保存（Android: SharedPreferences，iOS: UserDefaults）
5. **访客身份生效**：用户使用访客ID访问所有核心功能（血糖、饮食、运动、数字人）
6. **后续访问复用**：用户再次打开应用，从本地存储读取访客ID，继续使用

**架构说明**：步骤2-3在 KMM shared 模块中实现（跨平台），步骤4使用平台特定实现（expect/actual）

## 5. Functional Composition (Functions)

| Type | ID | Title | Description | Version |
|------|----|-------|-------------|--------|
| [NEW] | FUN-001 | 检测访客ID存在性 | 检查本地缓存中是否存在访客ID | v0.2 |
| [NEW] | FUN-002 | 生成访客ID | 前端生成唯一访客ID（`GUEST_<timestamp>_<random>`） | v0.2 |

## 6. Acceptance Criteria (Feature-level)

- **FAC-01**: Given 用户首次打开应用，When 客户端检测本地存储无访客ID，Then 自动生成唯一访客ID并存储（跨平台逻辑）
- **FAC-02**: Given 访客ID已生成，When 用户访问血糖/饮食/运动记录接口，Then 系统正常响应并关联访客ID
- **FAC-03**: Given 用户再次打开应用，When 本地存储中已有访客ID，Then 复用该ID，不重新生成
- **FAC-04**: Given 用户清除应用数据（Android）或卸载重装（iOS），When 再次打开应用，Then 生成新的访客ID，旧数据无法访问
- **FAC-05**: Given 用户使用无效或格式错误的访客ID，When 访问接口，Then 返回 400 BadRequest
- **FAC-06**: Given 共享模块在 Android 和 iOS 上运行，When 执行访客ID生成和检测，Then 行为一致（跨平台测试）

## 9. Design Artifacts Index
- Sequence Diagram: `/cdase/project/design/uml/FTR-001.sequence.puml`
- Package/Class Diagram: `/cdase/project/design/uml/FTR-001.class.puml`
- ADRs: _(待创建)_

## 10. Test & Acceptance Index
- Feature-level acceptance tests: `/shared/src/commonTest/kotlin/com/twelfth/feature/FTR001GuestIdGenerationTest.kt`
- Related Function tests: 
  - `/shared/src/commonTest/kotlin/com/twelfth/data/local/GuestIdManagerDetectTest.kt` (FUN-001)
  - `/shared/src/commonTest/kotlin/com/twelfth/data/local/GuestIdManagerGenerateTest.kt` (FUN-002)
- Android specific tests:
  - `/androidApp/src/androidTest/kotlin/com/twelfth/android/LocalStorageTest.kt` (测试 SharedPreferences 实现)

## 11. Gate Checklist (AI MUST enforce)

- [x] Required APIs discovered in `/api/modules/*.api.md` - 已注册在 `user.api.md`
- [x] No duplicate logic exists in current API Registries - 确认无重复

### Requirement Gate
- [x] Flow written and numbered - 已完成 6 步流程（简化版）
- [x] Feature-level I/O explicit - API Contract 已明确（简化为仅返回 guestId）
- [x] Functions list complete (IDs) - 2 个 Functions 已列出
- [x] FACs are testable and numbered - 5 个 FACs 已编号（已更新）

### Design Gate
- [x] Sequence diagram covers all steps - `/cdase/project/design/uml/FTR-001.sequence.puml`
- [x] Class/package diagram exists (if needed) - `/cdase/project/design/uml/FTR-001.class.puml`
- [x] All Functions have frozen API/SPI - FUN-001 和 FUN-002 已冻结

### Development Gate
- [ ] Each Function has contract tests indexed
- [ ] Code plans exist for impacted Functions

### Test Gate
- [ ] All Function contract tests pass
- [ ] All Feature contract tests pass

### Acceptance Gate
- [ ] FACs verified by executable tests
