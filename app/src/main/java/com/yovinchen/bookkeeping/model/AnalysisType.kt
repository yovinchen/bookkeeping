package com.yovinchen.bookkeeping.model

/**
 * 分析类型枚举
 * 定义记账应用中不同的数据分析视图类型
 * 
 * 用于在数据分析模块中区分不同的分析维度和图表类型
 */
enum class AnalysisType {
    EXPENSE,    // 支出分析，用于分析用户的支出情况
    INCOME,     // 收入分析，用于分析用户的收入情况
    TREND       // 趋势分析，用于分析用户收支随时间的变化趋势
}
