package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.YearMonth
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val bookkeepingDao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()
    private val memberDao = BookkeepingDatabase.getDatabase(application).memberDao()
    private val categoryDao = BookkeepingDatabase.getDatabase(application).categoryDao()

    private val _selectedRecordType = MutableStateFlow<TransactionType?>(null)
    val selectedRecordType: StateFlow<TransactionType?> = _selectedRecordType.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    val members = memberDao.getAllMembers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val allRecords = bookkeepingDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredRecords = combine(
        allRecords,
        _selectedRecordType,
        _selectedMonth,
        _selectedMember
    ) { records, selectedType, selectedMonth, selectedMember ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)

                val typeMatches = selectedType?.let { record.type == it } ?: true
                val monthMatches = recordYearMonth == selectedMonth
                val memberMatches = selectedMember?.let { record.memberId == it.id } ?: true

                monthMatches && memberMatches && typeMatches
            }
            .sortedByDescending { it.date }
            .groupBy { record ->
                Calendar.getInstance().apply { 
                    time = record.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val totalIncome = combine(
        allRecords,
        _selectedMonth,
        _selectedMember
    ) { records, selectedMonth, selectedMember ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)
                
                val monthMatches = recordYearMonth == selectedMonth
                val memberMatches = selectedMember?.let { record.memberId == it.id } ?: true
                val typeMatches = record.type == TransactionType.INCOME

                monthMatches && memberMatches && typeMatches
            }
            .sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalExpense = combine(
        allRecords,
        _selectedMonth,
        _selectedMember
    ) { records, selectedMonth, selectedMember ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)
                
                val monthMatches = recordYearMonth == selectedMonth
                val memberMatches = selectedMember?.let { record.memberId == it.id } ?: true
                val typeMatches = record.type == TransactionType.EXPENSE

                monthMatches && memberMatches && typeMatches
            }
            .sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun setSelectedMember(member: Member?) {
        _selectedMember.value = member
    }

    fun moveMonth(forward: Boolean) {
        _selectedMonth.value = if (forward) {
            _selectedMonth.value.plusMonths(1)
        } else {
            _selectedMonth.value.minusMonths(1)
        }
    }

    suspend fun getMemberById(memberId: Int): Member? {
        return memberDao.getMemberById(memberId)
    }

    fun addRecord(
        amount: Double,
        category: String,
        description: String,
        date: Date,
        type: TransactionType,
        memberId: Int?
    ) {
        viewModelScope.launch {
            val record = BookkeepingRecord(
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = date,
                memberId = memberId
            )
            bookkeepingDao.insertRecord(record)
        }
    }

    fun updateRecord(record: BookkeepingRecord) {
        viewModelScope.launch {
            bookkeepingDao.updateRecord(record)
        }
    }

    fun deleteRecord(record: BookkeepingRecord) {
        viewModelScope.launch {
            bookkeepingDao.deleteRecord(record)
        }
    }

    fun setSelectedRecordType(type: TransactionType?) {
        _selectedRecordType.value = type
    }
}
