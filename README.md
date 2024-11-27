# Bookkeeping App

一个基于 Jetpack Compose 开发的现代化记账应用。

## 项目概述

本项目是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 记账应用，采用 MVVM 架构，提供简洁直观的用户界面和丰富的记账功能。

## 主要特性

- 💰 收入/支出记录管理
- 📊 分类管理系统
- 📅 自定义日期选择器
- 📈 月度统计视图
- 🎨 Material 3 设计风格

## 技术栈

- 开发语言：Kotlin
- UI 框架：Jetpack Compose
- 架构模式：MVVM
- 数据存储：Room Database
- 依赖注入：Hilt
- 异步处理：Kotlin Coroutines

## 开发计划

### 1. 数据统计与可视化 (feature/statistics)
- [ ] 支出/收入趋势图表
- [ ] 分类占比饼图
- [ ] 月度/年度报表

### 2. 数据导出与备份 (feature/backup)
- [ ] 导出 CSV/Excel 功能
- [ ] 云端备份支持
- [ ] 数据迁移工具

### 3. 预算管理 (feature/budget)
- [ ] 月度预算设置
- [ ] 预算超支提醒
- [ ] 分类预算管理

### 4. 用户体验优化 (feature/ux-enhancement)
- [ ] 深色模式支持
- [ ] 手势操作优化
- [ ] 快速记账小组件
- [ ] 多语言支持

### 5. 性能优化 (feature/performance)
- [ ] 大数据量处理优化
- [ ] 启动速度优化
- [ ] 内存使用优化

## 分支管理

- `master`: 主分支，保持稳定可用
- `develop`: 开发分支，新功能开发的基础分支
- `feature/*`: 具体功能开发分支
- `release/*`: 发布准备分支

## 如何贡献

1. Fork 本仓库
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 许可证

本项目采用 GNU GPLv3 许可证 - 详见 [LICENSE](LICENSE) 文件
