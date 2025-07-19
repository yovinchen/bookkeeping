package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 预算实体类
 * 用于设置和跟踪月度预算、分类预算和成员预算
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    /**
     * 预算类型：TOTAL（总预算）, CATEGORY（分类预算）, MEMBER（成员预算）
     */
    val type: BudgetType,
    
    /**
     * 预算关联的分类名称（仅在 type 为 CATEGORY 时使用）
     */
    val categoryName: String? = null,
    
    /**
     * 预算关联的成员ID（仅在 type 为 MEMBER 时使用）
     */
    val memberId: Int? = null,
    
    /**
     * 预算金额
     */
    val amount: Double,
    
    /**
     * 预算生效开始日期
     */
    val startDate: Date,
    
    /**
     * 预算生效结束日期
     */
    val endDate: Date,
    
    /**
     * 是否启用此预算
     */
    val isEnabled: Boolean = true,
    
    /**
     * 提醒阈值（百分比，如 0.8 表示达到 80% 时提醒）
     */
    val alertThreshold: Double = 0.8,
    
    /**
     * 创建时间
     */
    val createdAt: Date = Date(),
    
    /**
     * 更新时间
     */
    val updatedAt: Date = Date()
)

/**
 * 预算类型枚举
 */
enum class BudgetType {
    TOTAL,      // 总预算
    CATEGORY,   // 分类预算
    MEMBER      // 成员预算
}

/**
 * 预算状态数据类，用于展示预算使用情况
 */
data class BudgetStatus(
    val budget: Budget,
    val spent: Double,          // 已花费金额
    val remaining: Double,      // 剩余金额
    val percentage: Double,     // 使用百分比
    val isOverBudget: Boolean,  // 是否超预算
    val isNearLimit: Boolean    // 是否接近预算限制
)