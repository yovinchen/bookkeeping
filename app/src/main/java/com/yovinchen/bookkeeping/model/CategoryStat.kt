package com.yovinchen.bookkeeping.model

data class CategoryStat(
    val category: String,
    val amount: Double,
    val count: Int = 0,
    val percentage: Double = 0.0
)
