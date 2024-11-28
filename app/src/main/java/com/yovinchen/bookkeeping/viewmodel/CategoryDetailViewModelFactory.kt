package com.yovinchen.bookkeeping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import java.time.YearMonth

class CategoryDetailViewModelFactory(
    private val database: BookkeepingDatabase,
    private val category: String,
    private val month: YearMonth
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryDetailViewModel(database, category, month) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
