package com.yovinchen.bookkeeping.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY dateTime DESC")
    fun getAllRecords(): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Record>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("SELECT SUM(amount) FROM records WHERE isExpense = :isExpense AND dateTime BETWEEN :startDate AND :endDate")
    fun getTotalAmountByType(isExpense: Boolean, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double?>

    @Query("SELECT * FROM records WHERE category = :category ORDER BY dateTime DESC")
    fun getRecordsByCategory(category: String): Flow<List<Record>>
}
