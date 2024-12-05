package com.yovinchen.bookkeeping.model

import androidx.room.ColumnInfo

data class MemberStat(
    @ColumnInfo(name = "member")
    val member: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "count")
    val count: Int,
    
    @ColumnInfo(name = "percentage")
    val percentage: Double = 0.0
)
