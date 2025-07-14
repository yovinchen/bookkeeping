package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yovinchen.bookkeeping.model.Member
import java.util.Date

/**
 * 交易类型枚举
 * 定义记账记录的交易类型
 */
enum class TransactionType {
    INCOME,   // 收入
    EXPENSE   // 支出
}

/**
 * Room数据库类型转换器
 * 用于在数据库中存储和检索复杂类型
 */
class Converters {
    /**
     * 将时间戳转换为Date对象
     * 
     * @param value 时间戳（毫秒）
     * @return 对应的Date对象，如果输入为null则返回null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * 将Date对象转换为时间戳
     * 
     * @param date Date对象
     * @return 对应的时间戳（毫秒），如果输入为null则返回null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * 将字符串转换为TransactionType枚举
     * 
     * @param value 交易类型的字符串表示
     * @return 对应的TransactionType枚举值
     */
    @TypeConverter
    fun fromTransactionType(value: String): TransactionType {
        return enumValueOf<TransactionType>(value)
    }

    /**
     * 将TransactionType枚举转换为字符串
     * 
     * @param type TransactionType枚举值
     * @return 对应的字符串表示
     */
    @TypeConverter
    fun transactionTypeToString(type: TransactionType): String {
        return type.name
    }
}

/**
 * 记账记录实体类
 * 用于在Room数据库中存储用户的收支记录
 * 
 * 该实体与Member实体存在外键关系，表示每条记录可以关联到一个家庭成员
 */
@Entity(
    tableName = "bookkeeping_records",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.SET_NULL  // 当关联的成员被删除时，将此字段设为NULL
        )
    ],
    indices = [
        Index(value = ["memberId"])  // 在memberId上创建索引以提高查询性能
    ]
)
@TypeConverters(Converters::class)  // 应用类型转换器
data class BookkeepingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,               // 记录ID，自动生成
    val amount: Double,             // 金额
    val type: TransactionType,      // 交易类型（收入或支出）
    val category: String,           // 分类
    val description: String,        // 描述
    val date: Date,                 // 日期
    val memberId: Int? = null       // 关联的成员ID，可为空表示未指定成员
)
