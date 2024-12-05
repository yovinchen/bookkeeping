package com.yovinchen.bookkeeping.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val isExpense: Boolean = true,
    val member: String = "Default"
)
