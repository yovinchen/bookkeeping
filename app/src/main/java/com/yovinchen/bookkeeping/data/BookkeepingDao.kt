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

    @Query("SELECT * FROM bookkeeping_records WHERE memberId = :memberId OR memberId IS NULL ORDER BY date DESC")
    fun getRecordsByMember(memberId: Int): Flow<List<BookkeepingRecord>>

    @Query("SELECT * FROM bookkeeping_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: Date, endDate: Date): Flow<List<BookkeepingRecord>>

    @Query("SELECT * FROM bookkeeping_records WHERE (memberId = :memberId OR memberId IS NULL) AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByMemberAndDateRange(memberId: Int, startDate: Date, endDate: Date): Flow<List<BookkeepingRecord>>

    @Query("SELECT * FROM bookkeeping_records WHERE type = :type ORDER BY date DESC")
    fun getRecordsByType(type: TransactionType): Flow<List<BookkeepingRecord>>

    @Query("SELECT SUM(amount) FROM bookkeeping_records WHERE type = :type AND (memberId = :memberId OR memberId IS NULL)")
    fun getTotalAmountByType(type: TransactionType, memberId: Int? = null): Flow<Double?>

    @Insert
    suspend fun insertRecord(record: BookkeepingRecord): Long

    @Update
    suspend fun updateRecord(record: BookkeepingRecord)

    @Delete
    suspend fun deleteRecord(record: BookkeepingRecord)

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT EXISTS(SELECT 1 FROM bookkeeping_records WHERE category = :categoryName LIMIT 1)")
    suspend fun isCategoryInUse(categoryName: String): Boolean

    @Query("UPDATE bookkeeping_records SET category = :newName WHERE category = :oldName")
    suspend fun updateRecordCategories(oldName: String, newName: String)
}
