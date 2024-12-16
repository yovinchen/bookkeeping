package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.utils.IconManager

private const val TAG = "CategoryManagementDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementDialog(
    onDismiss: () -> Unit,
    categories: List<Category>,
    onAddCategory: (String, TransactionType, Int?) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onUpdateCategory: (Category, String, Int?) -> Unit,
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("类别管理") },
        text = {
            Column {
                // 类型选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = type == selectedType,
                            onClick = { onTypeChange(type) },
                            label = { 
                                Text(when (type) {
                                    TransactionType.EXPENSE -> "支出"
                                    TransactionType.INCOME -> "收入"
                                })
                            }
                        )
                    }
                }

                // 类别列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(categories.filter { it.type == selectedType }) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { editingCategory = category },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // 显示类别图标
                                if (category.icon != null) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = category.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                } else {
                                    IconManager.getCategoryIconVector(category.name)?.let { icon ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Unspecified
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category.name)
                            }

                            IconButton(
                                onClick = { onDeleteCategory(category) },
                                enabled = categories.size > 1
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = if (categories.size > 1)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                }

                // 添加类别按钮
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加类别")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )

    // 添加类别对话框
    if (showAddDialog) {
        CategoryEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, iconResId ->
                onAddCategory(name, selectedType, iconResId)
                showAddDialog = false
            }
        )
    }

    // 编辑类别对话框
    editingCategory?.let { category ->
        CategoryEditDialog(
            onDismiss = { editingCategory = null },
            onConfirm = { name, iconResId ->
                onUpdateCategory(category, name, iconResId)
                editingCategory = null
            },
            initialName = category.name,
            initialIcon = category.icon
        )
    }
}

@Composable
private fun CategoryEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int?) -> Unit,
    initialName: String = "",
    initialIcon: Int? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "添加类别" else "编辑类别") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 图标选择按钮
                Button(
                    onClick = { showIconPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedIcon?.let { iconResId ->
                        Icon(
                            imageVector = ImageVector.vectorResource(id = iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (selectedIcon == null) "选择图标" else "更改图标")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { 
                selectedIcon = it
                showIconPicker = false
            },
            selectedIcon = selectedIcon,
            isMemberIcon = false,
            title = "选择类别图标"
        )
    }
}
