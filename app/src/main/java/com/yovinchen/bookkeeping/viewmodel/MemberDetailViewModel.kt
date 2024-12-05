package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class MemberDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BookkeepingDatabase.getDatabase(application)
    private val recordDao = database.bookkeepingDao()

    private val _memberRecords = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val memberRecords: StateFlow<List<BookkeepingRecord>> = _memberRecords

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    fun loadMemberRecords(memberName: String, category: String, yearMonth: YearMonth, analysisType: AnalysisType) {
        viewModelScope.launch {
            val startDate = yearMonth.atDay(1).atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .let { Date.from(it) }

            val endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .let { Date.from(it) }

            val transactionType = when (analysisType) {
                AnalysisType.INCOME -> TransactionType.INCOME
                AnalysisType.EXPENSE -> TransactionType.EXPENSE
                else -> null
            }

            val records = if (category.isEmpty()) {
                recordDao.getRecordsByMember(
                    memberName = memberName,
                    startDate = startDate,
                    endDate = endDate,
                    transactionType = transactionType
                )
            } else {
                recordDao.getRecordsByMemberAndCategory(
                    memberName = memberName,
                    category = category,
                    startDate = startDate,
                    endDate = endDate,
                    transactionType = transactionType
                )
            }
            _memberRecords.value = records
            _totalAmount.value = records.sumOf { it.amount }
        }
    }
}
