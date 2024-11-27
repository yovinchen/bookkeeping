package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BookkeepingDatabase.getDatabase(application)
    private val dao = database.bookkeepingDao()

    private val _selectedCategoryType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategoryType: StateFlow<TransactionType> = _selectedCategoryType.asStateFlow()

    val categories: StateFlow<List<Category>> = _selectedCategoryType
        .flatMapLatest { type ->
            dao.getCategoriesByType(type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedCategoryType(type: TransactionType) {
        _selectedCategoryType.value = type
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            val category = Category(name = name, type = type)
            dao.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    fun updateCategory(category: Category, newName: String) {
        viewModelScope.launch {
            val updatedCategory = category.copy(name = newName)
            dao.updateCategory(updatedCategory)
            // 更新所有使用该类别的记录
            dao.updateRecordCategories(category.name, newName)
        }
    }

    suspend fun isCategoryInUse(categoryName: String): Boolean {
        return dao.isCategoryInUse(categoryName)
    }
}
