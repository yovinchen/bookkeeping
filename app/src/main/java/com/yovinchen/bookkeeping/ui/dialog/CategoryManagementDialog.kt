package com.yovinchen.bookkeeping.ui.dialog

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType

private const val TAG = "CategoryManagementDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementDialog(
    onDismiss: () -> Unit,
    categories: List<Category>,
    onAddCategory: (String, TransactionType) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onUpdateCategory: (Category, String) -> Unit,
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCategory: Category? by remember { mutableStateOf(null) }
    var editingCategoryName by remember { mutableStateOf("") }
    val filteredCategories = categories.filter { it.type == selectedType }

    Log.d(TAG, "Dialog state - showDialog: $showDialog, showDeleteDialog: $showDeleteDialog")
    Log.d(TAG, "Selected category: ${selectedCategory?.name}")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                Log.d(TAG, "Main dialog dismiss requested")
                showDialog = false
                onDismiss()
            },
            title = { Text("类别管理") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // 类型选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { 
                                Log.d(TAG, "Switching to EXPENSE type")
                                onTypeChange(TransactionType.EXPENSE) 
                            },
                            label = { Text("支出") }
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { 
                                Log.d(TAG, "Switching to INCOME type")
                                onTypeChange(TransactionType.INCOME) 
                            },
                            label = { Text("收入") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 添加新类别
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("新类别名称") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    Log.d(TAG, "Adding new category: $newCategoryName")
                                    onAddCategory(newCategoryName, selectedType)
                                    newCategoryName = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加类别")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 类别列表
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedCategory = category
                                            editingCategoryName = category.name
                                            showEditDialog = true
                                        }
                                )
                                IconButton(
                                    onClick = { 
                                        Log.d(TAG, "Selected category for deletion: ${category.name}")
                                        selectedCategory = category
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除类别")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        Log.d(TAG, "Main dialog confirmed")
                        showDialog = false
                        onDismiss()
                    }
                ) {
                    Text("完成")
                }
            }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { 
                Log.d(TAG, "Delete dialog dismissed")
                showDeleteDialog = false
                selectedCategory = null
            },
            title = { Text("确认删除") },
            text = { 
                Text(
                    text = buildString {
                        append("确定要删除类别 ")
                        append(selectedCategory?.name ?: "")
                        append(" 吗？")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            selectedCategory?.let { category ->
                                Log.d(TAG, "Confirming deletion of category: ${category.name}")
                                onDeleteCategory(category)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error during category deletion callback", e)
                            e.printStackTrace()
                        } finally {
                            showDeleteDialog = false
                            selectedCategory = null
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        Log.d(TAG, "Canceling deletion")
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑类别对话框
    if (showEditDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                selectedCategory = null
                editingCategoryName = ""
            },
            title = { Text("编辑类别") },
            text = {
                OutlinedTextField(
                    value = editingCategoryName,
                    onValueChange = { editingCategoryName = it },
                    label = { Text("类别名称") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingCategoryName.isNotBlank()) {
                            selectedCategory?.let { category ->
                                onUpdateCategory(category, editingCategoryName)
                            }
                        }
                        showEditDialog = false
                        selectedCategory = null
                        editingCategoryName = ""
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        selectedCategory = null
                        editingCategoryName = ""
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
