package com.yovinchen.bookkeeping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CategoryDetailViewModel(
    private val database: BookkeepingDatabase,
    private val category: String,
    private val month: YearMonth
) : ViewModel() {
    private val _records = MutableStateFlow<List<BookkeepingRecord>>(emptyList())
    val records: StateFlow<List<BookkeepingRecord>> = _records

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members

    init {
        loadRecords()
        loadMembers()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            database.bookkeepingDao().getRecordsByCategoryAndMonth(category, monthStr)
                .collect { records ->
                    _records.value = records
                    _total.value = records.sumOf { it.amount }
                }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            database.memberDao().getAllMembers().collect { members ->
                _members.value = members
            }
        }
    }
}
