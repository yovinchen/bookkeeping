package com.yovinchen.bookkeeping.data

import androidx.room.*
import com.yovinchen.bookkeeping.model.Budget
import com.yovinchen.bookkeeping.model.BudgetType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 预算数据访问对象
 * 提供预算相关的数据库操作接口
 */
@Dao
interface BudgetDao {
    
    /**
     * 插入新预算
     */
    @Insert
    suspend fun insertBudget(budget: Budget): Long
    
    /**
     * 更新预算
     */
    @Update
    suspend fun updateBudget(budget: Budget)
    
    /**
     * 删除预算
     */
    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    /**
     * 根据ID获取预算
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): Budget?
    
    /**
     * 获取所有启用的预算
     */
    @Query("SELECT * FROM budgets WHERE isEnabled = 1 ORDER BY type, amount DESC")
    fun getAllEnabledBudgets(): Flow<List<Budget>>
    
    /**
     * 获取所有预算（包括禁用的）
     */
    @Query("SELECT * FROM budgets ORDER BY type, amount DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    /**
     * 根据类型获取预算
     */
    @Query("SELECT * FROM budgets WHERE type = :type AND isEnabled = 1")
    fun getBudgetsByType(type: BudgetType): Flow<List<Budget>>
    
    /**
     * 获取总预算
     */
    @Query("SELECT * FROM budgets WHERE type = 'TOTAL' AND isEnabled = 1 AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getTotalBudget(date: Date): Budget?
    
    /**
     * 获取指定分类的预算
     */
    @Query("SELECT * FROM budgets WHERE type = 'CATEGORY' AND categoryName = :categoryName AND isEnabled = 1 AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getCategoryBudget(categoryName: String, date: Date): Budget?
    
    /**
     * 获取指定成员的预算
     */
    @Query("SELECT * FROM budgets WHERE type = 'MEMBER' AND memberId = :memberId AND isEnabled = 1 AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getMemberBudget(memberId: Int, date: Date): Budget?
    
    /**
     * 获取当前有效的所有预算
     */
    @Query("SELECT * FROM budgets WHERE isEnabled = 1 AND :date BETWEEN startDate AND endDate ORDER BY type, amount DESC")
    fun getActiveBudgets(date: Date): Flow<List<Budget>>
    
    /**
     * 更新预算的启用状态
     */
    @Query("UPDATE budgets SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :budgetId")
    suspend fun updateBudgetEnabled(budgetId: Int, enabled: Boolean, updatedAt: Date = Date())
    
    /**
     * 删除所有过期的预算（可选）
     */
    @Query("DELETE FROM budgets WHERE endDate < :date AND isEnabled = 0")
    suspend fun deleteExpiredBudgets(date: Date)
    
    /**
     * 获取指定日期范围内的分类预算
     */
    @Query("SELECT * FROM budgets WHERE type = 'CATEGORY' AND isEnabled = 1 AND :date BETWEEN startDate AND endDate")
    fun getCategoryBudgetsForDate(date: Date): Flow<List<Budget>>
    
    /**
     * 获取指定日期范围内的成员预算
     */
    @Query("SELECT * FROM budgets WHERE type = 'MEMBER' AND isEnabled = 1 AND :date BETWEEN startDate AND endDate")
    fun getMemberBudgetsForDate(date: Date): Flow<List<Budget>>
}