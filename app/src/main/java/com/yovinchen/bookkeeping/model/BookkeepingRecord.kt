package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: String): TransactionType {
        return enumValueOf<TransactionType>(value)
    }

    @TypeConverter
    fun transactionTypeToString(type: TransactionType): String {
        return type.name
    }
}

@Entity(tableName = "bookkeeping_records")
@TypeConverters(Converters::class)
data class BookkeepingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val description: String,
    val date: Date
)
