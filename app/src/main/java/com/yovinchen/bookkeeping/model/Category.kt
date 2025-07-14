package com.yovinchen.bookkeeping.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 交易分类实体类
 * 用于在Room数据库中存储收支分类信息
 * 
 * 在记账应用中，每条记账记录都属于某个分类，
 * 如"餐饮"、"交通"、"工资"等，便于用户对支出和收入进行分类统计
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,               // 分类ID，自动生成
    val name: String,               // 分类名称
    val type: TransactionType,      // 分类关联的交易类型（收入或支出）
    val icon: Int? = null           // 分类图标资源ID，可选，默认为null
)
