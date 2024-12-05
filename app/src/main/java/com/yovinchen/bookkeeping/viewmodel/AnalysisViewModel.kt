package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.CategoryStat
import com.yovinchen.bookkeeping.model.MemberStat
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()
    private val memberDao = BookkeepingDatabase.getDatabase(application).memberDao()

    private val _startMonth = MutableStateFlow(YearMonth.now())
    val startMonth: StateFlow<YearMonth> = _startMonth.asStateFlow()

    private val _endMonth = MutableStateFlow(YearMonth.now())
    val endMonth: StateFlow<YearMonth> = _endMonth.asStateFlow()

    private val _selectedAnalysisType = MutableStateFlow(AnalysisType.EXPENSE)
    val selectedAnalysisType: StateFlow<AnalysisType> = _selectedAnalysisType.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStat>> = _categoryStats.asStateFlow()

    private val _memberStats = MutableStateFlow<List<MemberStat>>(emptyList())
    val memberStats: StateFlow<List<MemberStat>> = _memberStats.asStateFlow()

    init {
        viewModelScope.launch {
            combine(startMonth, endMonth, selectedAnalysisType) { start, end, type ->
                Triple(start, end, type)
            }.collect { (start, end, type) ->
                updateStats(start, end, type)
            }
        }
    }

    fun setStartMonth(month: YearMonth) {
        _startMonth.value = month
    }

    fun setEndMonth(month: YearMonth) {
        _endMonth.value = month
    }

    fun setAnalysisType(type: AnalysisType) {
        _selectedAnalysisType.value = type
    }

    private suspend fun updateStats(startMonth: YearMonth, endMonth: YearMonth, type: AnalysisType) {
        val records = recordDao.getAllRecords().first()
        val monthRecords = records.filter {
            val recordDate = Date(it.date.time)
            val localDateTime = LocalDateTime.ofInstant(recordDate.toInstant(), ZoneId.systemDefault())
            val yearMonth = YearMonth.from(localDateTime)
            yearMonth.isAfter(startMonth.minusMonths(1)) && 
            yearMonth.isBefore(endMonth.plusMonths(1)) && 
            it.type == when(type) {
                AnalysisType.EXPENSE -> TransactionType.EXPENSE
                AnalysisType.INCOME -> TransactionType.INCOME
                else -> null
            }
        }

        // 按分类统计
        val categoryMap = monthRecords.groupBy { it.category }
        val categoryStats = categoryMap.map { (category, records) ->
            CategoryStat(
                category = category,
                amount = records.sumOf { it.amount },
                count = records.size
            )
        }.sortedByDescending { it.amount }

        // 计算分类总额和百分比
        val categoryTotal = categoryStats.sumOf { it.amount }
        val categoryStatsWithPercentage = categoryStats.map { 
            it.copy(percentage = if (categoryTotal > 0) it.amount / categoryTotal * 100 else 0.0) 
        }

        // 按成员统计
        val members = memberDao.getAllMembers().first()
        val memberMap = monthRecords.groupBy { record ->
            members.find { it.id == record.memberId }?.name ?: "未分配"
        }
        
        val memberStats = memberMap.map { (memberName, records) ->
            MemberStat(
                member = memberName,
                amount = records.sumOf { it.amount },
                count = records.size
            )
        }.sortedByDescending { it.amount }

        // 计算成员总额和百分比
        val memberTotal = memberStats.sumOf { it.amount }
        val memberStatsWithPercentage = memberStats.map { 
            it.copy(percentage = if (memberTotal > 0) it.amount / memberTotal * 100 else 0.0) 
        }

        _categoryStats.value = categoryStatsWithPercentage
        _memberStats.value = memberStatsWithPercentage
    }
}
