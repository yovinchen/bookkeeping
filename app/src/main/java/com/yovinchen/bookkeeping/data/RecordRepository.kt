package com.yovinchen.bookkeeping.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class RecordRepository(private val recordDao: RecordDao) {
    fun getAllRecords(): Flow<List<Record>> = recordDao.getAllRecords()

    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Record>> =
        recordDao.getRecordsByDateRange(startDate, endDate)

    suspend fun insertRecord(record: Record) = recordDao.insertRecord(record)

    suspend fun updateRecord(record: Record) = recordDao.updateRecord(record)

    suspend fun deleteRecord(record: Record) = recordDao.deleteRecord(record)

    fun getTotalAmountByType(isExpense: Boolean, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double?> =
        recordDao.getTotalAmountByType(isExpense, startDate, endDate)

    fun getRecordsByCategory(category: String): Flow<List<Record>> =
        recordDao.getRecordsByCategory(category)
}
