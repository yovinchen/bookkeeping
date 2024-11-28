package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.CategoryStat
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()
    private val memberDao = BookkeepingDatabase.getDatabase(application).memberDao()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedAnalysisType = MutableStateFlow(AnalysisType.EXPENSE)
    val selectedAnalysisType = _selectedAnalysisType.asStateFlow()

    private val members = memberDao.getAllMembers()

    val memberStats = combine(selectedMonth, selectedAnalysisType, members) { month, type, membersList ->
        val records = recordDao.getAllRecords().first()
        val monthRecords = records.filter {
            val recordDate = Date(it.date.time)
            val localDateTime = LocalDateTime.ofInstant(recordDate.toInstant(), ZoneId.systemDefault())
            YearMonth.from(localDateTime) == month && it.type == when(type) {
                AnalysisType.EXPENSE -> TransactionType.EXPENSE
                AnalysisType.INCOME -> TransactionType.INCOME
                else -> null
            }
        }

        // 按成员统计
        val memberMap = monthRecords.groupBy { record ->
            membersList.find { it.id == record.memberId }?.name ?: "未分配"
        }
        
        val stats = memberMap.map { (memberName, records) ->
            CategoryStat(
                category = memberName,
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

    val categoryStats = combine(selectedMonth, selectedAnalysisType) { month, type ->
        val records = recordDao.getAllRecords().first()
        val monthRecords = records.filter {
            val recordDate = Date(it.date.time)
            val localDateTime = LocalDateTime.ofInstant(recordDate.toInstant(), ZoneId.systemDefault())
            YearMonth.from(localDateTime) == month && it.type == when(type) {
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
