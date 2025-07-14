package com.yovinchen.bookkeeping.model

import androidx.room.ColumnInfo

/**
 * 家庭成员统计数据类
 * 用于表示某个成员的统计信息，通常用于数据分析和图表展示
 * 
 * 该类不是数据库实体，而是通过数据库查询直接映射的结果类，
 * 表示按成员分组的聚合数据
 */
data class MemberStat(
    /**
     * 成员名称
     * 映射数据库查询结果中的member列
     */
    @ColumnInfo(name = "member")
    val member: String,
    
    /**
     * 该成员的总金额
     * 映射数据库查询结果中的amount列
     */
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    /**
     * 该成员下的记录数量
     * 映射数据库查询结果中的count列
     */
    @ColumnInfo(name = "count")
    val count: Int,
    
    /**
     * 该成员金额占总金额的百分比（0.0-100.0）
     * 映射数据库查询结果中的percentage列
     */
    @ColumnInfo(name = "percentage")
    val percentage: Double = 0.0
)
