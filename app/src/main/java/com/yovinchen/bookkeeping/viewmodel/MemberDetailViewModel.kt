package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class MemberDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BookkeepingDatabase.getDatabase(application)
    private val recordDao = database.bookkeepingDao()

    private val _memberRecords = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val memberRecords: StateFlow<List<BookkeepingRecord>> = _memberRecords.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    private val _categoryData = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val categoryData: StateFlow<List<Pair<String, Float>>> = _categoryData.asStateFlow()

    fun loadMemberRecords(
        memberName: String,
        category: String,
        startMonth: YearMonth,
        endMonth: YearMonth,
        analysisType: AnalysisType
    ) {
        val startDate = startMonth.atDay(1).atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }

        val endDate = endMonth.atEndOfMonth().atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }

        val transactionType = when (analysisType) {
            AnalysisType.INCOME -> TransactionType.INCOME
            AnalysisType.EXPENSE -> TransactionType.EXPENSE
            else -> null
        }

        val recordsFlow = if (category.isEmpty()) {
            recordDao.getRecordsByMemberAndDateRange(
                memberName = memberName,
                startDate = startDate,
                endDate = endDate,
                transactionType = transactionType
            )
        } else {
            recordDao.getRecordsByMemberCategoryAndDateRange(
                memberName = memberName,
                category = category,
                startDate = startDate,
                endDate = endDate,
                transactionType = transactionType
            )
        }

        viewModelScope.launch {
            recordsFlow.collect { records ->
                _memberRecords.value = records
                _totalAmount.value = records.sumOf { it.amount }
                
                // 计算分类数据
                val categoryAmounts = records.groupBy { it.category }
                    .mapValues { (_, records) -> records.sumOf { it.amount }.toFloat() }
                    .toList()
                    .sortedByDescending { it.second }
                _categoryData.value = categoryAmounts
            }
        }
    }
}
