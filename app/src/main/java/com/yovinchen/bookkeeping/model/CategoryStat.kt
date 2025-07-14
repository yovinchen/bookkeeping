package com.yovinchen.bookkeeping.model

/**
 * 分类统计数据类
 * 用于表示某个分类的统计信息，通常用于数据分析和图表展示
 * 
 * 该类不是数据库实体，而是从数据库查询结果中聚合生成的统计数据
 */
data class CategoryStat(
    val category: String,           // 分类名称
    val amount: Double,             // 该分类的总金额
    val count: Int = 0,             // 该分类下的记录数量
    val percentage: Double = 0.0    // 该分类金额占总金额的百分比（0.0-100.0）
)
