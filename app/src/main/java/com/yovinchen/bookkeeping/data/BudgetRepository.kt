package com.yovinchen.bookkeeping.data

import com.yovinchen.bookkeeping.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预算仓库类
 * 提供预算相关的业务逻辑和数据访问
 */
class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val bookkeepingDao: BookkeepingDao,
    private val memberDao: MemberDao
) {
    
    /**
     * 创建新预算
     */
    suspend fun createBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }
    
    /**
     * 更新预算
     */
    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.copy(updatedAt = Date()))
    }
    
    /**
     * 删除预算
     */
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }
    
    /**
     * 获取所有预算
     */
    fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets()
    }
    
    /**
     * 获取所有启用的预算
     */
    fun getAllEnabledBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllEnabledBudgets()
    }
    
    /**
     * 获取当前有效的预算状态
     */
    fun getActiveBudgetStatuses(date: Date = Date()): Flow<List<BudgetStatus>> {
        return combine(
            budgetDao.getActiveBudgets(date),
            bookkeepingDao.getAllRecords(),
            memberDao.getAllMembers()
        ) { budgets, records, members ->
            budgets.map { budget ->
                val spent = calculateSpent(budget, records, members, date)
                val remaining = budget.amount - spent
                val percentage = if (budget.amount > 0) spent / budget.amount else 0.0
                
                BudgetStatus(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    percentage = percentage,
                    isOverBudget = spent > budget.amount,
                    isNearLimit = percentage >= budget.alertThreshold
                )
            }
        }
    }
    
    /**
     * 计算预算已花费金额
     */
    private fun calculateSpent(
        budget: Budget,
        records: List<BookkeepingRecord>,
        members: List<Member>,
        currentDate: Date
    ): Double {
        // 只计算支出类型的记录
        val expenseRecords = records.filter { 
            it.type == TransactionType.EXPENSE &&
            it.date >= budget.startDate &&
            it.date <= budget.endDate &&
            it.date <= currentDate
        }
        
        return when (budget.type) {
            BudgetType.TOTAL -> {
                // 总预算：计算所有支出
                expenseRecords.sumOf { it.amount }
            }
            BudgetType.CATEGORY -> {
                // 分类预算：计算指定分类的支出
                expenseRecords
                    .filter { it.category == budget.categoryName }
                    .sumOf { it.amount }
            }
            BudgetType.MEMBER -> {
                // 成员预算：计算指定成员的支出
                expenseRecords
                    .filter { it.memberId == budget.memberId }
                    .sumOf { it.amount }
            }
        }
    }
    
    /**
     * 获取总预算状态
     */
    suspend fun getTotalBudgetStatus(date: Date = Date()): BudgetStatus? {
        val budget = budgetDao.getTotalBudget(date) ?: return null
        val records = bookkeepingDao.getAllRecords().first()
        val members = memberDao.getAllMembers().first()
        
        val spent = calculateSpent(budget, records, members, date)
        val remaining = budget.amount - spent
        val percentage = if (budget.amount > 0) spent / budget.amount else 0.0
        
        return BudgetStatus(
            budget = budget,
            spent = spent,
            remaining = remaining,
            percentage = percentage,
            isOverBudget = spent > budget.amount,
            isNearLimit = percentage >= budget.alertThreshold
        )
    }
    
    /**
     * 获取分类预算状态
     */
    fun getCategoryBudgetStatuses(date: Date = Date()): Flow<List<BudgetStatus>> {
        return combine(
            budgetDao.getCategoryBudgetsForDate(date),
            bookkeepingDao.getAllRecords(),
            memberDao.getAllMembers()
        ) { budgets, records, members ->
            budgets.map { budget ->
                val spent = calculateSpent(budget, records, members, date)
                val remaining = budget.amount - spent
                val percentage = if (budget.amount > 0) spent / budget.amount else 0.0
                
                BudgetStatus(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    percentage = percentage,
                    isOverBudget = spent > budget.amount,
                    isNearLimit = percentage >= budget.alertThreshold
                )
            }
        }
    }
    
    /**
     * 获取成员预算状态
     */
    fun getMemberBudgetStatuses(date: Date = Date()): Flow<List<BudgetStatus>> {
        return combine(
            budgetDao.getMemberBudgetsForDate(date),
            bookkeepingDao.getAllRecords(),
            memberDao.getAllMembers()
        ) { budgets, records, members ->
            budgets.map { budget ->
                val spent = calculateSpent(budget, records, members, date)
                val remaining = budget.amount - spent
                val percentage = if (budget.amount > 0) spent / budget.amount else 0.0
                
                BudgetStatus(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    percentage = percentage,
                    isOverBudget = spent > budget.amount,
                    isNearLimit = percentage >= budget.alertThreshold
                )
            }
        }
    }
    
    /**
     * 检查是否有预算超支或接近限制
     */
    suspend fun checkBudgetAlerts(date: Date = Date()): List<BudgetStatus> {
        val allStatuses = getActiveBudgetStatuses(date).first()
        return allStatuses.filter { it.isOverBudget || it.isNearLimit }
    }
    
    /**
     * 更新预算启用状态
     */
    suspend fun updateBudgetEnabled(budgetId: Int, enabled: Boolean) {
        budgetDao.updateBudgetEnabled(budgetId, enabled)
    }
    
    /**
     * 清理过期的禁用预算
     */
    suspend fun cleanupExpiredBudgets() {
        budgetDao.deleteExpiredBudgets(Date())
    }
}