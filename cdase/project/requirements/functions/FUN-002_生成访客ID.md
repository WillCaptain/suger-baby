> all information should be short, concise, accurate

# Function: FUN-002 生成访客ID

## 0. Metadata
- ID: FUN-002
- Owner: hangxiao
- Group/Module: User/GuestManagement
- Stage: Design
- Status: InProgress
- Stability: Experimental
- Priority: P0
- Version: v0.1
- Last Updated: 2026-01-29
- Depends On: 
  - 无（纯前端逻辑，生成唯一ID）

## 1. Summary (One Paragraph)

生成唯一的访客ID并存储到微信小程序本地缓存。访客ID格式为 `GUEST_<timestamp>_<random>`，其中 timestamp 为当前毫秒时间戳，random 为8位随机字符串。生成后的ID立即写入本地 Storage，确保后续访问可复用。

## 2. API / Method

### 前端方法（微信小程序）
- `GuestIdManager.generateGuestId() -> Promise<string>`
  - **Description**: 生成访客ID并存储到本地
  - **Params**: 无
  - **Returns**: 
    - `string`: 新生成的访客ID（格式：`GUEST_<timestamp>_<random>`）
  - **Throws**: 
    - `StorageError`: 本地存储失败

### ID 生成规则
- **格式**: `GUEST_<timestamp>_<random>`
- **timestamp**: 13位毫秒时间戳 (e.g., `1706543210123`)
- **random**: 8位随机字符串，字符集 `[a-z0-9]`
- **示例**: `GUEST_1706543210123_a3f9c2e1`

### 存储键
- **Storage Key**: `GUEST_ID`
- **存储位置**: 微信小程序 `wx.setStorageSync('GUEST_ID', guestId)`

## 3. Acceptance Criteria (Testable Contract)

- **AC-01**: Given 调用 `generateGuestId()`，When 生成成功，Then 返回格式为 `GUEST_<timestamp>_<random>` 的字符串
- **AC-02**: Given 调用 `generateGuestId()`，When 生成成功，Then 访客ID 已保存到本地 Storage（键: `GUEST_ID`）
- **AC-03**: Given 短时间内多次调用 `generateGuestId()`，When 生成多个ID，Then 每个ID的 timestamp 部分不同或 random 部分不同（保证唯一性）
- **AC-04**: Given 生成的访客ID，When 验证格式，Then timestamp 为13位数字，random 为8位小写字母或数字
- **AC-05**: Given 本地 Storage 写入失败，When 调用 `generateGuestId()`，Then 抛出 `StorageError` 异常

## 4. Error Handling & Edge Cases

- **E-01**: Storage 写入失败 → 抛出 `StorageError`，不返回访客ID
- **E-02**: 时间戳获取失败（极端情况） → 使用当前时间戳或抛出异常
- **E-03**: 随机数生成失败 → 重试最多3次，仍失败则抛出异常

## 5. Contract Tests Index

### Test File
- Path: `/tests/frontend/test_FUN_002_generate_guest_id.js`

### Test Cases
| AC ID | Test Name | Description | Input Set | Expected Output |
|------|-----------|-------------|-----------|-----------------|
| AC-01 | test_FUN_002_AC_01_valid_format | 格式正确 | 无 | 返回 `GUEST_*` 格式ID |
| AC-02 | test_FUN_002_AC_02_stored_in_cache | 已存储 | 无 | Storage 中存在该ID |
| AC-03 | test_FUN_002_AC_03_uniqueness | 唯一性 | 生成10次 | 10个ID互不相同 |
| AC-04 | test_FUN_002_AC_04_format_validation | 格式验证 | 无 | timestamp 13位，random 8位 |
| AC-05 | test_FUN_002_AC_05_storage_error | 存储异常 | Storage: mock error | 抛出 `StorageError` |

## 6. Gate Checklist (AI MUST enforce)

- [x] Required APIs discovered in `/api/modules/*.api.md` - 无外部API依赖
- [x] No duplicate logic exists in current API Registries - 确认无重复

### Requirement Gate
- [x] Inputs/Outputs complete
- [x] ACs are testable and numbered (5个AC)
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
- Tests: `/tests/frontend/test_FUN_002_generate_guest_id.js`
- Code Entry: `utils/GuestIdManager.generateGuestId()` (前端小程序)

## 10. Version History
- v0.1 (2026-01-29)
  - Type: Initial
  - Summary: 初始版本，定义访客ID生成逻辑
  - Changed ACs: N/A (首次创建)
  - Introduced Risks: 无

## 12. Risks & Non-Breakable Invariants

- **R-01**: 时钟回拨可能导致 timestamp 重复 (检测方式: 前端记录最后一次 timestamp，拒绝回拨)
- **R-02**: 随机数碰撞（极低概率） (检测方式: 后端接口验证访客ID唯一性)
- **INV-01**: 生成的ID格式必须严格符合 `GUEST_<13位数字>_<8位字符>` (测试强制: 正则表达式验证)
- **INV-02**: 生成后必须立即存储，不能延迟 (测试强制: 生成后立即验证 Storage)
