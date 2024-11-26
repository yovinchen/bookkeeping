package com.yovinchen.bookkeeping.data

import androidx.room.*
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BookkeepingDao {
    @Query("SELECT * FROM bookkeeping_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<BookkeepingRecord>>

    @Insert
    suspend fun insertRecord(record: BookkeepingRecord)

    @Delete
    suspend fun deleteRecord(record: BookkeepingRecord)

    @Update
    suspend fun updateRecord(record: BookkeepingRecord)

    @Query("SELECT * FROM bookkeeping_records WHERE type = 'INCOME'")
    fun getAllIncome(): Flow<List<BookkeepingRecord>>

    @Query("SELECT * FROM bookkeeping_records WHERE type = 'EXPENSE'")
    fun getAllExpense(): Flow<List<BookkeepingRecord>>

    // 按日期查询
    @Query("SELECT * FROM bookkeeping_records WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getRecordsByDate(startOfDay: Date, endOfDay: Date): Flow<List<BookkeepingRecord>>

    // 按日期范围查询
    @Query("SELECT * FROM bookkeeping_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: Date, endDate: Date): Flow<List<BookkeepingRecord>>

    // 按类别查询
    @Query("SELECT * FROM bookkeeping_records WHERE category = :category ORDER BY date DESC")
    fun getRecordsByCategory(category: String): Flow<List<BookkeepingRecord>>

    // 按类型查询
    @Query("SELECT * FROM bookkeeping_records WHERE type = :type ORDER BY date DESC")
    fun getRecordsByType(type: TransactionType): Flow<List<BookkeepingRecord>>

    // Category related queries
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT EXISTS(SELECT 1 FROM bookkeeping_records WHERE category = :categoryName LIMIT 1)")
    suspend fun isCategoryInUse(categoryName: String): Boolean

    @Query("UPDATE bookkeeping_records SET category = :newName WHERE category = :oldName")
    suspend fun updateRecordCategories(oldName: String, newName: String)
}
