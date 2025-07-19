package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.data.BudgetRepository
import com.yovinchen.bookkeeping.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.Calendar

/**
 * 预算管理 ViewModel
 * 负责预算相关的业务逻辑和状态管理
 */
class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BookkeepingDatabase.getDatabase(application)
    private val budgetRepository = BudgetRepository(
        database.budgetDao(),
        database.bookkeepingDao(),
        database.memberDao()
    )
    
    // 所有预算列表
    val allBudgets = budgetRepository.getAllBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 当前活跃的预算状态
    val activeBudgetStatuses = budgetRepository.getActiveBudgetStatuses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 总预算状态
    private val _totalBudgetStatus = MutableStateFlow<BudgetStatus?>(null)
    val totalBudgetStatus: StateFlow<BudgetStatus?> = _totalBudgetStatus.asStateFlow()
    
    // 分类预算状态
    val categoryBudgetStatuses = budgetRepository.getCategoryBudgetStatuses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 成员预算状态
    val memberBudgetStatuses = budgetRepository.getMemberBudgetStatuses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 编辑中的预算
    private val _editingBudget = MutableStateFlow<Budget?>(null)
    val editingBudget: StateFlow<Budget?> = _editingBudget.asStateFlow()
    
    // 对话框显示状态
    private val _showBudgetDialog = MutableStateFlow(false)
    val showBudgetDialog: StateFlow<Boolean> = _showBudgetDialog.asStateFlow()
    
    init {
        // 初始化时加载总预算状态
        loadTotalBudgetStatus()
    }
    
    /**
     * 加载总预算状态
     */
    private fun loadTotalBudgetStatus() {
        viewModelScope.launch {
            _totalBudgetStatus.value = budgetRepository.getTotalBudgetStatus()
        }
    }
    
    /**
     * 创建新预算
     */
    fun createBudget(
        type: BudgetType,
        amount: Double,
        categoryName: String? = null,
        memberId: Int? = null,
        alertThreshold: Double = 0.8
    ) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startDate = calendar.time
            
            // 设置结束日期为当月最后一天
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endDate = calendar.time
            
            val budget = Budget(
                type = type,
                amount = amount,
                categoryName = categoryName,
                memberId = memberId,
                startDate = startDate,
                endDate = endDate,
                alertThreshold = alertThreshold
            )
            
            budgetRepository.createBudget(budget)
            loadTotalBudgetStatus()
        }
    }
    
    /**
     * 更新预算
     */
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
            loadTotalBudgetStatus()
        }
    }
    
    /**
     * 删除预算
     */
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
            loadTotalBudgetStatus()
        }
    }
    
    /**
     * 切换预算启用状态
     */
    fun toggleBudgetEnabled(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudgetEnabled(budget.id, !budget.isEnabled)
            loadTotalBudgetStatus()
        }
    }
    
    /**
     * 显示预算编辑对话框
     */
    fun showEditBudgetDialog(budget: Budget? = null) {
        _editingBudget.value = budget
        _showBudgetDialog.value = true
    }
    
    /**
     * 隐藏预算编辑对话框
     */
    fun hideBudgetDialog() {
        _showBudgetDialog.value = false
        _editingBudget.value = null
    }
    
    /**
     * 检查预算警报
     */
    fun checkBudgetAlerts() {
        viewModelScope.launch {
            val alerts = budgetRepository.checkBudgetAlerts()
            // 这里可以触发通知或其他警报机制
            // 暂时先不实现，等完成通知功能后再补充
        }
    }
    
    /**
     * 清理过期预算
     */
    fun cleanupExpiredBudgets() {
        viewModelScope.launch {
            budgetRepository.cleanupExpiredBudgets()
        }
    }
}