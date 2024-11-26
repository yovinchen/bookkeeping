package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.AppDatabase
import com.yovinchen.bookkeeping.data.Record
import com.yovinchen.bookkeeping.data.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecordRepository
    val allRecords: Flow<List<Record>>

    init {
        val recordDao = AppDatabase.getDatabase(application).recordDao()
        repository = RecordRepository(recordDao)
        allRecords = repository.getAllRecords()
    }

    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Record>> =
        repository.getRecordsByDateRange(startDate, endDate)

    fun insertRecord(record: Record) = viewModelScope.launch {
        repository.insertRecord(record)
    }

    fun updateRecord(record: Record) = viewModelScope.launch {
        repository.updateRecord(record)
    }

    fun deleteRecord(record: Record) = viewModelScope.launch {
        repository.deleteRecord(record)
    }

    fun getTotalAmountByType(isExpense: Boolean, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double?> =
        repository.getTotalAmountByType(isExpense, startDate, endDate)

    fun getRecordsByCategory(category: String): Flow<List<Record>> =
        repository.getRecordsByCategory(category)
}
