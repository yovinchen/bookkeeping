package com.yovinchen.bookkeeping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.MemberStat
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class CategoryDetailViewModel(
    private val database: BookkeepingDatabase,
    private val category: String,
    private val startMonth: YearMonth,
    private val endMonth: YearMonth
) : ViewModel() {
    private val recordDao = database.bookkeepingDao()

    private val _records = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val records: StateFlow<List<BookkeepingRecord>> = _records.asStateFlow()

    private val _memberStats = MutableStateFlow<List<MemberStat>>(emptyList())
    val memberStats: StateFlow<List<MemberStat>> = _memberStats.asStateFlow()

    val total: StateFlow<Double> = records
        .map { records -> records.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    init {
        val startDate = startMonth.atDay(1).atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }

        val endDate = endMonth.atEndOfMonth().atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }

        recordDao.getRecordsByCategoryAndDateRange(
            category = category,
            startDate = startDate,
            endDate = endDate
        )
        .onEach { records -> _records.value = records }
        .launchIn(viewModelScope)

        recordDao.getMemberStatsByCategoryAndDateRange(
            category = category,
            startDate = startDate,
            endDate = endDate
        )
        .onEach { stats -> _memberStats.value = stats }
        .launchIn(viewModelScope)
    }
}
