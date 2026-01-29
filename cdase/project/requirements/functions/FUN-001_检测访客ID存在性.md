> all information should be short, concise, accurate

# Function: FUN-001 检测访客ID存在性

## 0. Metadata
- ID: FUN-001
- Owner: hangxiao
- Group/Module: User/GuestManagement
- Stage: Design
- Status: InProgress
- Stability: Experimental
- Priority: P0
- Version: v0.1
- Last Updated: 2026-01-29
- Depends On: 
  - 无（纯前端逻辑，读取本地缓存）

## 1. Summary (One Paragraph)

检查微信小程序本地存储（Storage）中是否存在访客ID。这是访客身份管理的第一步，用于判断用户是首次访问还是重复访问。如果本地缓存中存在有效的访客ID，则复用该ID；否则触发访客ID生成流程。

## 2. API / Method

### 前端方法（微信小程序）
- `GuestIdManager.detectGuestId() -> Promise<string | null>`
  - **Description**: 从本地 Storage 读取访客ID
  - **Params**: 无
  - **Returns**: 
    - `string`: 访客ID（格式：`GUEST_<timestamp>_<random>`）
    - `null`: 本地无访客ID
  - **Throws**: 无（读取失败返回 null）

### 存储键
- **Storage Key**: `GUEST_ID`
- **存储位置**: 微信小程序 `wx.getStorageSync('GUEST_ID')`

## 3. Acceptance Criteria (Testable Contract)

- **AC-01**: Given 本地 Storage 中存在键 `GUEST_ID`，When 调用 `detectGuestId()`，Then 返回该访客ID字符串
- **AC-02**: Given 本地 Storage 中不存在键 `GUEST_ID`，When 调用 `detectGuestId()`，Then 返回 `null`
- **AC-03**: Given 本地 Storage 中访客ID格式无效（不符合 `GUEST_*` 模式），When 调用 `detectGuestId()`，Then 返回 `null` 并清除无效数据
- **AC-04**: Given 读取 Storage 发生异常，When 调用 `detectGuestId()`，Then 返回 `null` 并记录错误日志

## 4. Error Handling & Edge Cases

- **E-01**: Storage 读取失败 → 捕获异常，返回 `null`，记录日志
- **E-02**: 访客ID格式无效 → 清除无效数据，返回 `null`
- **E-03**: 访客ID为空字符串 → 返回 `null`

## 5. Contract Tests Index

### Test File
- Path: `/tests/frontend/test_FUN_001_detect_guest_id.js`

### Test Cases
| AC ID | Test Name | Description | Input Set | Expected Output |
|------|-----------|-------------|-----------|-----------------|
| AC-01 | test_FUN_001_AC_01_valid_guest_id_exists | 有效访客ID存在 | Storage: `GUEST_1706543210_abc123` | 返回 `GUEST_1706543210_abc123` |
| AC-02 | test_FUN_001_AC_02_no_guest_id | 无访客ID | Storage: empty | 返回 `null` |
| AC-03 | test_FUN_001_AC_03_invalid_format | 格式无效 | Storage: `invalid_id` | 返回 `null`，清除数据 |
| AC-04 | test_FUN_001_AC_04_storage_error | Storage异常 | Storage: throws error | 返回 `null`，记录日志 |

## 6. Gate Checklist (AI MUST enforce)

- [x] Required APIs discovered in `/api/modules/*.api.md` - 无外部API依赖
- [x] No duplicate logic exists in current API Registries - 确认无重复

### Requirement Gate
- [x] Inputs/Outputs complete
- [x] ACs are testable and numbered (4个AC)
- [x] Edge cases listed (3个边缘情况)

### Design Gate
- [x] API/SPI frozen and explicit
- [ ] Linked sequence diagram exists
- [x] Risks documented

### Development Gate
- [ ] Contract tests generated and indexed here

### Test Gate
- [ ] All contract tests pass

### Acceptance Gate
- [ ] All contract tests pass

## 7. Trace Links

- Feature: `/cdase/project/requirements/features/FTR-001_访客ID生成与管理.md`
- Sequence: `/cdase/project/design/uml/FTR-001.sequence.puml`
- Tests: `/tests/frontend/test_FUN_001_detect_guest_id.js`
- Code Entry: `utils/GuestIdManager.detectGuestId()` (前端小程序)

## 10. Version History
- v0.1 (2026-01-29)
  - Type: Initial
  - Summary: 初始版本，定义访客ID检测逻辑
  - Changed ACs: N/A (首次创建)
  - Introduced Risks: 无

## 12. Risks & Non-Breakable Invariants

- **R-01**: 用户清除小程序缓存导致访客ID丢失 (检测方式: 监控访客ID重复生成率)
- **INV-01**: 检测逻辑必须是同步的，不能有网络请求 (测试强制: 单元测试不允许 mock 网络)
