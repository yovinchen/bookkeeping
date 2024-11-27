package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedAnalysisType = MutableStateFlow(AnalysisType.EXPENSE)
    val selectedAnalysisType = _selectedAnalysisType.asStateFlow()

    val categoryStats = combine(selectedMonth, selectedAnalysisType) { month, type ->
        val records = recordDao.getAllRecords().first()
        val monthRecords = records.filter {
            val recordDate = LocalDateTime.ofInstant(it.date.toInstant(), ZoneId.systemDefault())
            YearMonth.from(recordDate) == month && it.type == when(type) {
                AnalysisType.EXPENSE -> TransactionType.EXPENSE
                AnalysisType.INCOME -> TransactionType.INCOME
                else -> null
            }
        }

        // 按分类统计
        val categoryMap = monthRecords.groupBy { it.category }
        val stats = categoryMap.map { (category, records) ->
            CategoryStat(
                category = category,
                amount = records.sumOf { it.amount },
                count = records.size
            )
        }.sortedByDescending { it.amount }

        // 计算总额
        val total = stats.sumOf { it.amount }
        
        // 计算百分比
        stats.map { it.copy(percentage = if (total > 0) it.amount / total * 100 else 0.0) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun setAnalysisType(type: AnalysisType) {
        _selectedAnalysisType.value = type
    }
}

enum class AnalysisType {
    EXPENSE, INCOME, TREND
}

data class CategoryStat(
    val category: String,
    val amount: Double,
    val count: Int,
    val percentage: Double = 0.0
)
