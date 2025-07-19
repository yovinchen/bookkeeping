package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val monthStartDay: Int = 1, // 月度开始日期，1-28，默认为1号
    val themeMode: String = "FOLLOW_SYSTEM", // 主题模式：FOLLOW_SYSTEM, LIGHT, DARK
    val autoBackupEnabled: Boolean = false, // 自动备份开关
    val autoBackupInterval: Int = 7, // 自动备份间隔（天）
    val lastBackupTime: Long = 0L, // 上次备份时间
    val encryptBackup: Boolean = true // 备份加密开关，默认开启
)