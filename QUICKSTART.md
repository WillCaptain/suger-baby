# 糖小稳 - 快速入门指南

## 📋 前置要求

- ✅ **JDK 17+** - 必需
- ✅ **Android Studio Hedgehog (2023.1.1)+** - 推荐
- ✅ **Android SDK** - API Level 24+ (Android 7.0+)
- ✅ **Gradle 8.2** - 通过 wrapper 自动下载

## 🚀 快速开始

### 1️⃣ 打开项目

```bash
cd c:\work\surger\suger-baby
```

在 Android Studio 中打开项目：
- `File` → `Open` → 选择项目根目录

### 2️⃣ 同步 Gradle

Android Studio 会自动提示同步 Gradle，点击 `Sync Now`。

或手动执行：
```bash
./gradlew sync
```

### 3️⃣ 运行测试

#### 运行 Shared 模块单元测试
```bash
./gradlew shared:testDebugUnitTest
```

#### 运行 Android 集成测试（需要连接设备或模拟器）
```bash
./gradlew androidApp:connectedAndroidTest
```

### 4️⃣ 构建应用

```bash
./gradlew androidApp:assembleDebug
```

生成的 APK 位于：`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### 5️⃣ 安装到设备

```bash
./gradlew androidApp:installDebug
```

或在 Android Studio 中点击运行按钮 ▶️

---

## 📱 功能验证

### FTR-001: 访客ID生成与管理

1. **首次打开应用**
   - 应用自动生成访客ID
   - 在主界面看到访客ID卡片
   - 格式：`GUEST_<timestamp>_<random>`
   - 示例：`GUEST_1706543210123_a3f9c2e1`

2. **关闭并重新打开应用**
   - 访客ID保持不变（复用）
   - Logcat 中应显示 "Detected existing guest ID"

3. **清除应用数据**
   - `设置` → `应用` → `糖小稳` → `清除数据`
   - 重新打开应用
   - 生成新的访客ID（与之前不同）

---

## 🧪 测试报告

### 查看单元测试报告
```bash
./gradlew shared:testDebugUnitTest
open shared/build/reports/tests/testDebugUnitTest/index.html
```

### 查看 Android 集成测试报告
```bash
./gradlew androidApp:connectedAndroidTest
open androidApp/build/reports/androidTests/connected/index.html
```

---

## 📂 项目结构速览

```
TangXiaoWen/
├── shared/                # KMM 共享模块（跨平台）
│   ├── commonMain/       # 业务逻辑（Android + iOS 共享）
│   ├── androidMain/      # Android 特定实现
│   └── commonTest/       # 跨平台测试
└── androidApp/           # Android 应用（Jetpack Compose）
    ├── main/             # 主代码
    └── androidTest/      # Android 集成测试
```

---

## 🐛 常见问题

### Q1: Gradle 同步失败

**解决方案**:
```bash
# 清理 Gradle 缓存
./gradlew clean

# 重新同步
./gradlew sync --refresh-dependencies
```

### Q2: 测试失败

**检查**:
- JDK 版本是否为 17+
- Android SDK 是否安装了 API Level 24+
- 对于 Android 测试，确保设备或模拟器已连接

### Q3: 应用闪退

**调试**:
```bash
# 查看 Logcat
adb logcat | grep "twelfth"
```

---

## 📖 文档索引

- **需求文档**: `cdase/project/requirements/features/FTR-001_访客ID生成与管理.md`
- **代码计划**: `cdase/project/code_plan/FTR-001.code_plan.md`
- **设计文档**: `cdase/project/design/uml/`
- **运行日志**: `cdase/project/run_log/run_log_2026013030.md`

---

## 🎯 下一步

1. ✅ 验证 FTR-001 功能
2. [ ] 实现 FTR-002: 访客转正与数据迁移
3. [ ] 实现 FTR-003: 隐私协议管理
4. [ ] 添加后端 API 集成
5. [ ] iOS 平台支持 (P2)

---

## 💬 获取帮助

如有问题，请查看：
- 📘 [完整 README](README.md)
- 📝 [项目约定](cdase/project/context/convention.context.md)
- 🏗️ [代码计划](cdase/project/code_plan/FTR-001.code_plan.md)

---

**祝你开发愉快！** 🚀
