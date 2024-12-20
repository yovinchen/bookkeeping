package com.yovinchen.bookkeeping.data

import androidx.room.*
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.MemberStat
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

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE category = :category 
        AND strftime('%Y-%m', datetime(date/1000, 'unixepoch')) = :yearMonth
        ORDER BY date DESC
    """)
    fun getRecordsByCategoryAndMonth(
        category: String,
        yearMonth: String
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE memberId IN (SELECT id FROM members WHERE name = :memberName)
        AND strftime('%Y-%m', datetime(date/1000, 'unixepoch')) = :yearMonth
        ORDER BY date DESC
    """)
    fun getRecordsByMemberAndMonth(
        memberName: String,
        yearMonth: String
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT 
            m.name as member,
            SUM(r.amount) as amount,
            COUNT(*) as count,
            (SUM(r.amount) * 100.0 / (SELECT SUM(amount) FROM bookkeeping_records WHERE category = :category AND strftime('%Y-%m', datetime(date/1000, 'unixepoch')) = :yearMonth)) as percentage
        FROM bookkeeping_records r
        JOIN members m ON r.memberId = m.id
        WHERE r.category = :category
        AND strftime('%Y-%m', datetime(r.date/1000, 'unixepoch')) = :yearMonth
        GROUP BY m.name
        ORDER BY amount DESC
    """)
    fun getMemberStatsByCategory(
        category: String,
        yearMonth: String
    ): Flow<List<MemberStat>>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE category = :category
        ORDER BY date DESC
    """)
    fun getRecordsByCategory(
        category: String
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE category = :category 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getRecordsByCategoryAndDateRange(
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE memberId IN (SELECT id FROM members WHERE name = :memberName)
        AND date BETWEEN :startDate AND :endDate
        AND (:transactionType IS NULL OR type = :transactionType)
        ORDER BY date DESC
    """)
    fun getRecordsByMemberAndDateRange(
        memberName: String,
        startDate: Date,
        endDate: Date,
        transactionType: TransactionType?
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE memberId IN (SELECT id FROM members WHERE name = :memberName)
        AND category = :category
        AND date BETWEEN :startDate AND :endDate
        AND (:transactionType IS NULL OR type = :transactionType)
        ORDER BY date DESC
    """)
    fun getRecordsByMemberCategoryAndDateRange(
        memberName: String,
        category: String,
        startDate: Date,
        endDate: Date,
        transactionType: TransactionType?
    ): Flow<List<BookkeepingRecord>>

    @Query("""
        SELECT 
            m.name as member,
            SUM(r.amount) as amount,
            COUNT(*) as count,
            (SUM(r.amount) * 100.0 / (SELECT SUM(amount) FROM bookkeeping_records WHERE category = :category AND date BETWEEN :startDate AND :endDate)) as percentage
        FROM bookkeeping_records r
        JOIN members m ON r.memberId = m.id
        WHERE r.category = :category
        AND r.date BETWEEN :startDate AND :endDate
        GROUP BY m.name
        ORDER BY amount DESC
    """)
    fun getMemberStatsByCategoryAndDateRange(
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<MemberStat>>

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

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE memberId IN (SELECT id FROM members WHERE name = :memberName) 
        AND date BETWEEN :startDate AND :endDate
        AND (
            :transactionType IS NULL 
            OR type = (
                CASE :transactionType
                    WHEN 'INCOME' THEN 'INCOME'
                    WHEN 'EXPENSE' THEN 'EXPENSE'
                END
            )
        )
        ORDER BY date DESC
    """)
    suspend fun getRecordsByMember(
        memberName: String, 
        startDate: Date, 
        endDate: Date,
        transactionType: TransactionType?
    ): List<BookkeepingRecord>

    @Query("""
        SELECT * FROM bookkeeping_records 
        WHERE memberId IN (SELECT id FROM members WHERE name = :memberName)
        AND category = :category 
        AND date BETWEEN :startDate AND :endDate
        AND (
            :transactionType IS NULL 
            OR type = (
                CASE :transactionType
                    WHEN 'INCOME' THEN 'INCOME'
                    WHEN 'EXPENSE' THEN 'EXPENSE'
                END
            )
        )
        ORDER BY date DESC
    """)
    suspend fun getRecordsByMemberAndCategory(
        memberName: String,
        category: String,
        startDate: Date,
        endDate: Date,
        transactionType: TransactionType?
    ): List<BookkeepingRecord>
}
