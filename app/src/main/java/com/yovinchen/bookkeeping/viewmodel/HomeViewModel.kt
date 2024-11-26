package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HomeViewModel"
    private val database = BookkeepingDatabase.getDatabase(application)
    private val dao = database.bookkeepingDao()

    val records = dao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _selectedCategoryType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategoryType: StateFlow<TransactionType> = _selectedCategoryType.asStateFlow()

    private val _selectedRecordType = MutableStateFlow<TransactionType?>(null)
    val selectedRecordType: StateFlow<TransactionType?> = _selectedRecordType.asStateFlow()

    private val _selectedDateTime = MutableStateFlow(LocalDateTime.now())
    val selectedDateTime: StateFlow<LocalDateTime> = _selectedDateTime.asStateFlow()

    val categories: StateFlow<List<Category>> = _selectedCategoryType
        .flatMapLatest { type ->
            dao.getCategoriesByType(type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredRecords = combine(records, selectedRecordType) { records, type ->
        when (type) {
            null -> records.sortedByDescending { it.date }
            else -> records.filter { it.type == type }.sortedByDescending { it.date }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            records.collect { recordsList ->
                updateTotals(recordsList)
            }
        }
    }

    private fun updateTotals(records: List<BookkeepingRecord>) {
        _totalIncome.value = records
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        _totalExpense.value = records
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
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

    fun setSelectedCategoryType(type: TransactionType) {
        _selectedCategoryType.value = type
    }

    fun setSelectedRecordType(type: TransactionType?) {
        _selectedRecordType.value = type
    }

    fun resetSelectedDateTime() {
        _selectedDateTime.value = LocalDateTime.now()
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            val category = Category(name = name, type = type)
            dao.insertCategory(category)
        }
    }

    fun updateCategory(category: Category, newName: String) {
        viewModelScope.launch {
            dao.updateCategory(category.copy(name = newName))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
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

    // 获取指定类别的记录
    fun getRecordsByCategory(category: String): Flow<List<BookkeepingRecord>> {
        return dao.getRecordsByCategory(category)
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
