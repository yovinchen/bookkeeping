package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 家庭成员实体类
 * 用于在Room数据库中存储家庭成员信息
 * 
 * 在记账应用中，每条记账记录可以关联到特定的家庭成员，
 * 以便追踪不同成员的收支情况
 */
@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                // 成员ID，自动生成
    val name: String,               // 成员姓名
    val description: String = "",   // 成员描述信息，可选，默认为空字符串
    val icon: Int? = null           // 成员图标资源ID，可选，默认为null
)
