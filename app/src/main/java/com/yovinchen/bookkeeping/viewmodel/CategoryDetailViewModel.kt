package com.yovinchen.bookkeeping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.CategoryStat
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CategoryDetailViewModel(
    private val database: BookkeepingDatabase,
    private val category: String,
    private val month: YearMonth
) : ViewModel() {
    private val recordDao = database.bookkeepingDao()
    private val yearMonthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private val _records = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val records: StateFlow<List<BookkeepingRecord>> = _records.asStateFlow()

    private val _memberStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val memberStats: StateFlow<List<CategoryStat>> = _memberStats.asStateFlow()

    val total: StateFlow<Double> = records
        .map { records -> records.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    init {
        recordDao.getRecordsByCategory(category)
            .onEach { records ->
                _records.value = records.filter { record ->
                    val recordMonth = YearMonth.from(
                        DateTimeFormatter.ofPattern("yyyy-MM")
                            .parse(yearMonthStr)
                    )
                    YearMonth.from(record.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()) == recordMonth
                }
            }
            .launchIn(viewModelScope)

        recordDao.getMemberStatsByCategory(category, yearMonthStr)
            .onEach { stats ->
                _memberStats.value = stats
            }
            .launchIn(viewModelScope)
    }
}
