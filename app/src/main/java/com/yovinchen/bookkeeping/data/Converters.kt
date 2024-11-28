package com.yovinchen.bookkeeping.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let {
            return LocalDateTime.parse(it, formatter)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromDate(value: Date?): String? {
        return value?.time?.toString()
    }

    @TypeConverter
    fun toDate(timestamp: String?): Date? {
        return timestamp?.let { Date(it.toLong()) }
    }
}
