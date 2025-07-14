# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个名为"轻记账"的 Android 记账应用，使用 Kotlin 和 Jetpack Compose 开发，采用 MVVM 架构模式。应用完全离线运行，注重用户隐私保护。

## 常用开发命令

### 构建命令
```bash
# 清理项目
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行所有构建任务
./gradlew build
```

### 测试命令
```bash
# 运行单元测试
./gradlew test

# 运行仪器测试
./gradlew connectedAndroidTest

# 运行特定模块的测试
./gradlew :app:test
```

### 代码检查
```bash
# 运行 lint 检查
./gradlew lint

# 查看 lint 报告（生成在 app/build/reports/lint-results.html）
./gradlew lintDebug
```

## 项目架构

### 核心架构模式：MVVM + Repository
- **View (UI层)**：使用 Jetpack Compose 构建的 UI 组件
- **ViewModel**：处理业务逻辑和状态管理
- **Model (数据层)**：Room Database + Repository 模式
- **依赖注入**：手动依赖注入（未使用 Hilt/Dagger）

### 主要技术栈
- **UI框架**：Jetpack Compose with Material 3
- **数据库**：Room 2.6.1
- **异步处理**：Kotlin Coroutines + Flow
- **导航**：Navigation Compose
- **图表**：MPAndroidChart 3.1.0
- **文件处理**：OpenCSV 5.7.1, Apache POI 5.2.3

### 包结构说明
```
com.yovinchen.bookkeeping/
├── data/              # 数据层：数据库、DAO、Repository
├── model/             # 数据模型：实体类和数据类
├── ui/                # UI 层
│   ├── components/    # 可复用的 UI 组件
│   ├── dialog/        # 对话框组件
│   ├── screen/        # 屏幕页面（HomeScreen, AnalysisScreen等）
│   └── theme/         # 主题配置
├── utils/             # 工具类
└── viewmodel/         # ViewModel 层
```

### 数据流架构
1. **UI 层**通过 ViewModel 获取数据和发送事件
2. **ViewModel** 通过 Repository 访问数据，使用 StateFlow 管理 UI 状态
3. **Repository** 封装数据访问逻辑，统一数据源接口
4. **Room Database** 提供本地数据持久化

### 关键设计决策
1. **单 Activity 架构**：整个应用只有一个 MainActivity，所有页面通过 Compose Navigation 管理
2. **状态管理**：使用 ViewModel + StateFlow 进行状态管理，确保配置变更时的数据保持
3. **主题系统**：支持深色/浅色模式切换和自定义主题色，通过 CompositionLocal 传递主题状态
4. **权限最小化**：仅在需要时请求必要权限（如文件读写权限）

## 开发规范

### Git 分支管理
- `master`：稳定主分支
- `develop`：主开发分支
- `feature/*`：功能开发分支
- `release/*`：版本发布分支
- `hotfix/*`：紧急修复分支

### 提交信息格式
使用约定式提交：`<type>: <description>`

类型包括：feat, fix, docs, style, refactor, perf, test, build, ci, chore

### 版本管理
- 版本号在 `app/build.gradle.kts` 中的 `versionName` 和 `versionCode` 管理
- APK 命名格式：`轻记账_${buildType}_v${versionName}_${date}.apk`

## 关键功能实现

### 记账记录管理
- 数据模型：`BookkeepingRecord` 实体类
- 存储：Room Database 自动管理
- 展示：按日期分组，支持编辑和删除

### 成员系统
- 成员数据存储在独立表中
- 记账记录通过成员 ID 列表关联
- 支持成员消费统计和分析

### 数据导入导出
- CSV 导出：使用 OpenCSV 库
- Excel 导出：使用 Apache POI 库
- 文件选择：通过 SAF (Storage Access Framework)

### 图表分析
- 使用 MPAndroidChart 库
- 通过 AndroidView 在 Compose 中集成
- 支持饼图、折线图等多种图表类型

## 注意事项

1. **数据库迁移**：修改数据库结构时必须提供 Migration
2. **Compose 预览**：使用 `@Preview` 注解时需要提供默认参数
3. **性能优化**：大量数据时注意使用分页或懒加载
4. **主题适配**：新增 UI 组件时确保同时支持深色和浅色主题
5. **图标资源**：项目包含大量自定义图标，位于 `drawable` 目录