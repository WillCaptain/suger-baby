# 糖小稳 (TangXiaoWen)

一个专为糖尿病患者设计的健康管理应用，使用 Kotlin Multiplatform Mobile (KMM) 架构。

## 项目结构

```
TangXiaoWen/
├── shared/                          # KMM 共享模块（跨平台）
│   ├── src/
│   │   ├── commonMain/             # 共享代码（Android + iOS）
│   │   │   └── kotlin/com/twelfth/
│   │   │       └── data/local/     # 数据层
│   │   │           ├── ILocalStorage.kt         # 存储接口
│   │   │           ├── LocalStorage.kt          # expect 类
│   │   │           ├── GuestIdManager.kt        # 访客ID管理器
│   │   │           └── StorageException.kt
│   │   ├── androidMain/            # Android 特定实现
│   │   │   └── kotlin/com/twelfth/
│   │   │       └── data/local/
│   │   │           ├── LocalStorage.android.kt  # SharedPreferences 实现
│   │   │           └── TimeUtils.android.kt
│   │   └── commonTest/             # 共享测试
│   │       └── kotlin/com/twelfth/
│   │           ├── data/local/     # 单元测试
│   │           │   ├── GuestIdManagerDetectTest.kt
│   │           │   └── GuestIdManagerGenerateTest.kt
│   │           └── feature/        # Feature 级别测试
│   │               └── FTR001GuestIdGenerationTest.kt
│   └── build.gradle.kts
├── androidApp/                      # Android 应用
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/twelfth/android/
│       │   │   ├── MainActivity.kt
│       │   │   └── ui/theme/
│       │   │       ├── Theme.kt
│       │   │       └── Type.kt
│       │   ├── res/
│       │   │   └── values/
│       │   │       ├── strings.xml
│       │   │       └── themes.xml
│       │   └── AndroidManifest.xml
│       └── androidTest/            # Android 集成测试
│           └── kotlin/com/twelfth/android/
│               └── LocalStorageTest.kt
├── cdase/                          # 项目文档
│   └── project/
│       ├── requirements/           # 需求文档
│       │   ├── features/
│       │   │   └── FTR-001_访客ID生成与管理.md
│       │   └── functions/
│       │       ├── FUN-001_检测访客ID存在性.md
│       │       └── FUN-002_生成访客ID.md
│       ├── design/                 # 设计文档
│       │   └── uml/
│       │       ├── FTR-001.class.puml
│       │       └── FTR-001.sequence.puml
│       └── context/                # 项目约定
│           └── convention.context.md
├── build.gradle.kts                # 项目级 Gradle 配置
├── settings.gradle.kts             # Gradle 设置
└── gradle.properties               # Gradle 属性
```

## 技术栈

### 共享模块 (Shared)
- **语言**: Kotlin Multiplatform
- **并发**: Kotlin Coroutines
- **本地存储**: expect/actual 模式
  - Android: SharedPreferences
  - iOS: UserDefaults (P2)

### Android 应用
- **UI 框架**: Jetpack Compose
- **架构组件**: ViewModel, Navigation
- **依赖注入**: Hilt (待添加)
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 34 (Android 14)

## 已实现功能

### FTR-001: 访客ID生成与管理 ✅

允许用户无需注册即可使用应用。系统自动生成访客ID并存储在本地。

#### 包含的 Functions:
- **FUN-001**: 检测访客ID存在性
- **FUN-002**: 生成访客ID

#### 访客ID格式:
```
GUEST_<timestamp>_<random>
例如: GUEST_1706543210123_a3f9c2e1
```

- `timestamp`: 13位毫秒时间戳
- `random`: 8位随机字符串 ([a-z0-9])

## 开发环境设置

### 前置要求
- **JDK**: 17 或更高
- **Android Studio**: Hedgehog (2023.1.1) 或更高
- **Gradle**: 8.2 (通过 wrapper 自动下载)

### 构建项目

1. **克隆仓库**
```bash
git clone <repository-url>
cd suger-baby
```

2. **同步 Gradle**
```bash
./gradlew sync
```

3. **运行测试**
```bash
# 运行共享模块测试
./gradlew shared:testDebugUnitTest

# 运行 Android 集成测试（需要模拟器或真机）
./gradlew androidApp:connectedAndroidTest
```

4. **构建 Android 应用**
```bash
./gradlew androidApp:assembleDebug
```

5. **安装到设备**
```bash
./gradlew androidApp:installDebug
```

## 测试策略

### 测试层次
1. **单元测试** (commonTest)
   - GuestIdManagerDetectTest (FUN-001)
   - GuestIdManagerGenerateTest (FUN-002)

2. **Feature 测试** (commonTest)
   - FTR001GuestIdGenerationTest (FTR-001)

3. **平台集成测试** (androidTest)
   - LocalStorageTest (SharedPreferences 实现)

### 运行所有测试
```bash
# 共享模块单元测试
./gradlew shared:test

# Android 集成测试（需要设备）
./gradlew androidApp:connectedAndroidTest
```

## 验收标准 (FAC)

### FTR-001 验收标准

- ✅ **FAC-01**: 首次打开应用时自动生成访客ID
- ✅ **FAC-02**: 访客ID可用于访问功能
- ✅ **FAC-03**: 重复访问复用现有访客ID
- ✅ **FAC-04**: 清除数据后生成新访客ID
- ✅ **FAC-05**: 无效访客ID被拒绝并清除
- ✅ **FAC-06**: 跨平台行为一致

## 包名约定

- **基础包名**: `com.twelfth` (代表"十二斋")
- **共享模块**: `com.twelfth.*`
- **Android 应用**: `com.twelfth.android.*`

## 下一步 (Roadmap)

### P0 (当前阶段)
- [x] FTR-001: 访客ID生成与管理
- [ ] FTR-002: 访客转正与数据迁移
- [ ] FTR-003: 隐私协议管理

### P1
- [ ] 手机号 + 验证码登录
- [ ] 血糖记录功能
- [ ] 饮食记录功能
- [ ] 运动记录功能

### P2
- [ ] iOS 平台支持
- [ ] 数字人对话功能
- [ ] 支付功能

## 许可证

Copyright © 2026 十二斋 (Twelfth)
