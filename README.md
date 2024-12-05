# 轻记账 (Lightweight Bookkeeping)

一个轻量级的个人记账应用，专注于隐私和离线使用。

## 📖 项目概述

本项目是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 记账应用，采用 MVVM 架构，提供简洁直观的用户界面和丰富的记账功能。

## ⭐️ 主要特性

- 🔒 完全离线运行，无需网络连接
- 📱 极简权限要求，仅使用必要的系统权限
- 💰 支持收入和支出记录
- 👥 支持多人记账
- 📊 按日期和类别统计

## 🛠 技术栈

- 💻 开发语言：Kotlin
- 🎨 UI 框架：Jetpack Compose
- 🏗️ 架构模式：MVVM
- 💾 数据存储：Room Database
- 💉 依赖注入：Hilt
- ⚡️ 异步处理：Kotlin Coroutines

## 🗺 开发路线图

### 0. 基础记账 (已完成 ✨)
- [x] 收入/支出记录管理
- [x] 分类管理系统
- [x] 自定义日期选择器
- [x] Material 3 设计界面
- [x] 深色/浅色主题切换
- [x] 主题色自定义

### 1. 成员系统 (已完成 🎉)
- [x] 成员添加/编辑/删除
- [x] 记账时选择相关成员
- [x] 主页账单修改相关成员
- [x] 成员消费统计

### 2. 图表分析 (进行中 🚀)
- [x] 支出/收入趋势图表
- [x] 分类占比饼图
- [ ] 月度/年度报表
- [x] 成员消费分析
- [x] 自定义统计周期

### 3. 数据管理 (计划中 📝)
- [ ] 导出 CSV/Excel 功能
- [ ] 数据迁移工具
- [ ] 定期自动备份
- [ ] 备份加密功能

### 4. 预算管理 (计划中 💡)
- [ ] 月度预算设置
- [ ] 预算超支提醒
- [ ] 分类预算管理
- [ ] 成员预算管理
- [ ] 预算分析报告

### 5. 体验优化 (持续进行 🔄)
- [x] 深色模式支持
- [ ] 手势操作优化
- [ ] 快速记账小组件
- [ ] 多语言支持
- [ ] 自定义主题

### 6. 性能提升 (持续进行 ⚡️)
- [ ] 大数据量处理优化
- [ ] 启动速度优化
- [ ] 内存使用优化
- [ ] 缓存策略优化
- [ ] 数据库查询优化

## 🌲 分支管理

- `master`: 稳定主分支
- `develop`: 主开发分支
- `feature/*`: 功能开发分支
- `release/*`: 版本发布分支
- `hotfix/*`: 紧急修复分支

## 🔄 提交规范

提交信息应遵循以下格式：`<type>: <description>`

### 提交类型（Type）

- `feat`: 新功能（feature）
- `fix`: 修复bug
- `docs`: 文档更新（documentation）
- `style`: 代码格式（不影响代码运行的变动）
- `refactor`: 代码重构（既不是新增功能，也不是修复bug）
- `perf`: 性能优化
- `test`: 测试相关
- `build`: 构建相关
- `ci`: 持续集成
- `chore`: 构建过程或辅助工具的变动
- `revert`: 回退提交
- `improvement`: 改进

## 📝 版本历史

### v1.2.0 - v1.2.4
- 分类数据可视化
  - 支出/收入分类饼图展示
  - 分类占比详细统计
  - 分类数据交互和筛选
- 成员数据可视化
  - 成员消费饼图展示
  - 成员支出占比统计
  - 成员数据交互和筛选
- 趋势分析
  - 日收支趋势折线图
  - 收入支出双线对比
  - 支持深色/浅色主题
  - 图表交互和缩放
- 数据筛选
  - 支持按日期范围筛选
  - 支持按收入/支出类型筛选
  - 支持按成员/分类筛选

### v1.1.0
- 成员管理功能
  - 成员添加/编辑/删除
  - 记账时选择相关成员
  - 成员消费统计
- UI/UX 优化
  - 记录展示优化
  - 月度统计界面
  - 分组展示优化
- 数据管理
  - 记录筛选增强
  - 数据库性能优化
  - 状态管理重构

### v1.0.0
- 基础记账功能
  - 收入/支出记录
  - 金额、日期、分类、备注管理
- Material 3 设计界面
  - 深色/浅色主题切换
  - 主题色自定义
- 分类管理
  - 默认分类预设
  - 自定义分类支持
  - 分类编辑与删除
- 月度统计
  - 月度收支总览
  - 月份快速切换
- 自定义日期选择器

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详细信息

## 📮 联系方式

- 作者：YovinChen
- 邮箱：gzh298255@gmail.com
- 博客：[blog.hhdxw.top](https://blog.hhdxw.top)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！
