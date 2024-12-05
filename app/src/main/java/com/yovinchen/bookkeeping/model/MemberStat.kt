package com.yovinchen.bookkeeping.model

data class MemberStat(
    val member: String,
    val amount: Double,
    val count: Int,
    val percentage: Double = 0.0
)
