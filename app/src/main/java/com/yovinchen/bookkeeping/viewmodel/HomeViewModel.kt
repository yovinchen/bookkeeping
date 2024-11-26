package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.YearMonth
import java.util.Date
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HomeViewModel"
    private val dao = BookkeepingDatabase.getDatabase(application).bookkeepingDao()

    private val _selectedRecordType = MutableStateFlow<TransactionType?>(null)
    val selectedRecordType: StateFlow<TransactionType?> = _selectedRecordType.asStateFlow()

    private val _selectedDateTime = MutableStateFlow(LocalDateTime.now())
    val selectedDateTime: StateFlow<LocalDateTime> = _selectedDateTime.asStateFlow()

    private val _selectedCategoryType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategoryType: StateFlow<TransactionType> = _selectedCategoryType.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val records = dao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = _selectedCategoryType
        .flatMapLatest { type ->
            dao.getCategoriesByType(type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredRecords = combine(
        records,
        _selectedRecordType,
        _selectedMonth
    ) { records, selectedType, selectedMonth ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)
                
                val typeMatches = selectedType?.let { record.type == it } ?: true
                val monthMatches = recordYearMonth == selectedMonth
                
                typeMatches && monthMatches
            }
            .sortedByDescending { it.date }
            .groupBy { record ->
                val calendar = Calendar.getInstance().apply { time = record.date }
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val totalIncome = combine(
        records,
        _selectedMonth
    ) { records, selectedMonth ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)
                
                record.type == TransactionType.INCOME && recordYearMonth == selectedMonth
            }
            .sumOf { it.amount }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    val totalExpense = combine(
        records,
        _selectedMonth
    ) { records, selectedMonth ->
        records
            .filter { record ->
                val recordDate = record.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val recordYearMonth = YearMonth.from(recordDate)
                
                record.type == TransactionType.EXPENSE && recordYearMonth == selectedMonth
            }
            .sumOf { it.amount }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    private fun updateTotals() {
        // 移除未使用的参数
    }

    init {
        viewModelScope.launch {
            records.collect {
                updateTotals()
            }
        }
    }

    fun addRecord(type: TransactionType, amount: Double, category: String, description: String) {
        viewModelScope.launch {
            val record = BookkeepingRecord(
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = Date.from(_selectedDateTime.value.atZone(ZoneId.systemDefault()).toInstant())
            )
            dao.insertRecord(record)
            resetSelectedDateTime()
        }
    }

    fun setSelectedDateTime(dateTime: LocalDateTime) {
        _selectedDateTime.value = dateTime
    }

    fun setSelectedRecordType(type: TransactionType?) {
        _selectedRecordType.value = type
    }

    fun setSelectedCategoryType(type: TransactionType) {
        _selectedCategoryType.value = type
    }

    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun moveMonth(forward: Boolean) {
        val current = _selectedMonth.value
        _selectedMonth.value = if (forward) {
            current.plusMonths(1)
        } else {
            current.minusMonths(1)
        }
    }

    fun resetSelectedDateTime() {
        _selectedDateTime.value = LocalDateTime.now()
    }

    fun updateRecord(record: BookkeepingRecord) {
        viewModelScope.launch {
            dao.updateRecord(record)
        }
    }

    fun deleteRecord(record: BookkeepingRecord) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }

    // 获取指定日期的记录
    fun getRecordsByDate(date: LocalDateTime): Flow<List<BookkeepingRecord>> {
        val calendar = Calendar.getInstance().apply {
            time = Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time
        return dao.getRecordsByDateRange(startOfDay, endOfDay)
    }

    // 获取指定日期范围的记录
    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BookkeepingRecord>> {
        val start = Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant())
        val end = Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant())
        return dao.getRecordsByDateRange(start, end)
    }

    // 获取指定类型的记录
    fun getRecordsByType(type: TransactionType): Flow<List<BookkeepingRecord>> {
        return dao.getRecordsByType(type)
    }
}

data class UiState(
    val isAddingRecord: Boolean = false,
    val isManagingCategories: Boolean = false
)
