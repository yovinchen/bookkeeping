# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

轻记账是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 记账应用，采用 MVVM 架构。项目强调隐私保护，完全离线运行。

## 常用命令

### 构建和运行
```bash
./gradlew build              # 构建项目
./gradlew assembleDebug      # 构建调试 APK
./gradlew assembleRelease    # 构建发布 APK
./gradlew installDebug       # 安装调试版本到设备
./gradlew clean             # 清理构建产物
```

### 测试相关
```bash
./gradlew test              # 运行单元测试
./gradlew connectedAndroidTest  # 运行设备测试
```

## 架构概览

### MVVM 架构分层
- **Model 层**: Room 数据库实体 (`model/` 目录下的 BookkeepingRecord, Category, Member, Settings)
- **Data 层**: DAO 接口和 Repository 模式实现数据访问
- **ViewModel 层**: 每个功能模块独立的 ViewModel (HomeViewModel, AnalysisViewModel 等)
- **View 层**: Jetpack Compose UI，包含 screens、components 和 dialogs

### 核心数据流
1. UI 层通过 ViewModel 发起数据请求
2. ViewModel 调用 Repository 获取数据
3. Repository 通过 DAO 访问 Room 数据库
4. 数据通过 Flow/StateFlow 响应式传递回 UI

### 数据库架构
- **主数据库**: BookkeepingDatabase (当前版本 5)
- **核心表**: bookkeeping_records, categories, members, settings
- **关键关系**: records 通过 memberId 关联 members 表
- **迁移策略**: 使用 Room 的 Migration 机制处理版本升级

### 导航架构
使用 Jetpack Navigation Compose，主要页面：
- HomeScreen: 主页记账列表
- AnalysisScreen: 数据分析图表
- AddRecordScreen: 添加/编辑记录
- CategoryManagementScreen: 分类管理
- MemberManagementScreen: 成员管理
- SettingsScreen: 设置页面

## 关键技术决策

### UI 框架
- 完全使用 Jetpack Compose 构建 UI
- Material 3 设计系统，支持深色/浅色主题
- 自定义主题色功能通过 ColorThemeDialog 实现

### 数据可视化
- 使用 MPAndroidChart 实现图表功能
- ChartManager 统一管理图表样式和行为
- 支持饼图和折线图两种展示方式

### 文件导入导出
- Apache POI 处理 Excel 文件
- OpenCSV 处理 CSV 文件
- FileUtils 提供统一的文件操作接口

### 图标系统
- 所有图标使用 Material Icons 和自定义矢量图标
- CategoryIcon 和 MemberIcon 枚举管理图标映射
- 支持动态图标选择和预览

## 开发注意事项

### 分支策略
- master: 稳定主分支
- develop: 开发分支
- feature/*: 功能开发
- release/*: 版本发布
- hotfix/*: 紧急修复

### 提交规范
使用约定式提交: `<type>: <description>`
- feat: 新功能
- fix: 修复 bug
- docs: 文档更新
- style: 代码格式
- refactor: 代码重构
- perf: 性能优化
- test: 测试相关
- build: 构建相关

### 重要功能模块

#### 月度记账开始日期 (Settings 中的 monthStartDay)
- 支持自定义每月记账的开始日期 (1-31)
- 影响月度统计和分析的日期范围计算
- 默认值为 1 (每月 1 日开始)

#### 加密功能 (EncryptionUtils)
- 预留的备份加密功能接口
- 使用 AES 加密算法
- 目前尚未集成到备份功能中

#### 默认数据初始化
- 首次启动时自动创建默认分类
- 包含常用的收入和支出分类
- 可通过 insertDefaultCategories() 查看默认分类列表

### 性能考虑
- 使用 Room 的 Flow 实现数据的响应式更新
- 图表数据计算在 ViewModel 中异步处理
- 大量数据导入时使用批量插入优化性能