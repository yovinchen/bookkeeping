package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MemberDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()

    private val _memberRecords = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val memberRecords: StateFlow<List<BookkeepingRecord>> = _memberRecords.asStateFlow()

    val totalAmount: StateFlow<Double> = _memberRecords
        .map { records -> records.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun loadMemberRecords(memberName: String, yearMonth: YearMonth) {
        recordDao.getRecordsByMemberAndMonth(
            memberName,
            yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        ).onEach { records ->
            _memberRecords.value = records
        }.launchIn(viewModelScope)
    }
}
