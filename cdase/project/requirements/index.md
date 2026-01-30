# Requirements Index

> **Authority**: Constitution §XIV
> **Purpose**: 所有需求产物的权威索引

此文件是所有需求产物的权威索引。

只有非 `Done` 状态的产物被视为活跃任务。

---

## Scenarios

| Scenario ID | Description | Status | Owner | Phase |
| ----------- | ----------- | ------ | ----- | ----- |
| SCN-001 | 访客快速体验 | **In Progress** | hangxiao | 第一阶段（2周） |
| SCN-002 | 血糖记录与展示 | Draft | **Will** | 第一阶段（2周） |
| SCN-003 | 个人信息与病例管理 | Draft | hangxiao | 第一阶段（2周） |
| SCN-004 | 数字人智能对话与引导 | Draft | hangxiao | 第二阶段（3周） |
| SCN-005 | 饮食拍照识别与记录 | Draft | hangxiao | 第三阶段（3周） |
| SCN-006 | 运动记录与消耗计算 | Draft | hangxiao | 第三阶段（3周） |
| SCN-007 | 首页健康数据聚合展示 | Draft | hangxiao | 第三阶段（3周） |
| SCN-008 | 数字人健康问诊 | Draft | hangxiao | 第四阶段（2周） |
| SCN-009 | 订阅套餐与支付 | Draft | hangxiao | 第四阶段（2周） |

---

## Features

| Feature ID | Description | Status | Owner | Path |
| ----------- | ----------- | ------ | ----- | ------ |
| FTR-001 | 访客ID生成与管理 | **Design Complete** | hangxiao | [features/FTR-001_访客ID生成与管理.md](features/FTR-001_访客ID生成与管理.md) |
| FTR-002 | 访客转正与数据迁移 | **Deferred (P1)** | hangxiao | [features/FTR-002_访客转正与数据迁移.md](features/FTR-002_访客转正与数据迁移.md) |
| FTR-003 | 隐私协议管理 | **Deferred (P1)** | hangxiao | [features/FTR-003_隐私协议管理.md](features/FTR-003_隐私协议管理.md) |

---

## Functions

| Function ID | Description | Status | Owner | Path |
| ----------- | ----------- | ------ | ----- | ----- |
| FUN-001 | 检测访客ID存在性 | Design Complete | hangxiao | [functions/FUN-001_检测访客ID存在性.md](functions/FUN-001_检测访客ID存在性.md) |
| FUN-002 | 生成访客ID | Design Complete | hangxiao | [functions/FUN-002_生成访客ID.md](functions/FUN-002_生成访客ID.md) |

---

**Last Updated**: 2026-01-30 10:30  
**Updated By**: hangxiao  
**Total Scenarios**: 9 (全部为 P0 优先级)  
**Total Features**: 3 (FTR-001 Design Complete, FTR-002/003 延期到 P1)  
**Total Functions**: 2 (FUN-001, FUN-002 已完成设计)  
**Active Assignees**: hangxiao (8 scenarios, 1 feature, 2 functions), Will (1 scenario)  
**Platform**: Android 原生应用 (Kotlin + Jetpack)  
**Git Status**: ✅ 已同步到远程仓库
