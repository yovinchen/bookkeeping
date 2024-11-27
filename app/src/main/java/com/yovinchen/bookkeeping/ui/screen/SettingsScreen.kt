package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.components.ColorPicker
import com.yovinchen.bookkeeping.ui.components.predefinedColors
import com.yovinchen.bookkeeping.ui.dialog.CategoryManagementDialog
import com.yovinchen.bookkeeping.ui.dialog.MemberManagementDialog
import com.yovinchen.bookkeeping.viewmodel.MemberViewModel
import com.yovinchen.bookkeeping.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    memberViewModel: MemberViewModel = viewModel()
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showMemberDialog by remember { mutableStateOf(false) }
    
    val categories by viewModel.categories.collectAsState()
    val selectedType by viewModel.selectedCategoryType.collectAsState()
    val members by memberViewModel.allMembers.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        // 成员管理设置项
        ListItem(
            headlineContent = { Text("成员管理") },
            supportingContent = { Text("管理账本成员") },
            modifier = Modifier.clickable { showMemberDialog = true }
        )

        Divider()

        // 类别管理设置项
        ListItem(
            headlineContent = { Text("类别管理") },
            supportingContent = { Text("管理收入和支出类别") },
            modifier = Modifier.clickable { showCategoryDialog = true }
        )

        Divider()

        // 主题设置项
        ListItem(
            headlineContent = { Text("主题设置") },
            supportingContent = {
                Text(
                    when (currentTheme) {
                        is ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                        is ThemeMode.LIGHT -> "浅色"
                        is ThemeMode.DARK -> "深色"
                        is ThemeMode.CUSTOM -> "自定义颜色"
                    }
                )
            },
            modifier = Modifier.clickable { showThemeDialog = true }
        )

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("选择主题") },
                text = {
                    Column {
                        // 基本主题选项
                        ThemeOption(
                            text = "跟随系统",
                            selected = currentTheme is ThemeMode.FOLLOW_SYSTEM,
                            onClick = { 
                                onThemeChange(ThemeMode.FOLLOW_SYSTEM)
                                showThemeDialog = false 
                            }
                        )
                        
                        ThemeOption(
                            text = "浅色",
                            selected = currentTheme is ThemeMode.LIGHT,
                            onClick = { 
                                onThemeChange(ThemeMode.LIGHT)
                                showThemeDialog = false 
                            }
                        )
                        
                        ThemeOption(
                            text = "深色",
                            selected = currentTheme is ThemeMode.DARK,
                            onClick = { 
                                onThemeChange(ThemeMode.DARK)
                                showThemeDialog = false 
                            }
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // 颜色选择器
                        Text(
                            text = "自定义颜色",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        ColorPicker(
                            selectedColor = when (currentTheme) {
                                is ThemeMode.CUSTOM -> currentTheme.primaryColor
                                else -> predefinedColors[0]
                            },
                            onColorSelected = { color ->
                                onThemeChange(ThemeMode.CUSTOM(color))
                                showThemeDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }

    // 类别管理对话框
    if (showCategoryDialog) {
        CategoryManagementDialog(
            onDismiss = { showCategoryDialog = false },
            categories = categories,
            onAddCategory = viewModel::addCategory,
            onDeleteCategory = viewModel::deleteCategory,
            onUpdateCategory = viewModel::updateCategory,
            selectedType = selectedType,
            onTypeChange = viewModel::setSelectedCategoryType
        )
    }

    // 成员管理对话框
    if (showMemberDialog) {
        MemberManagementDialog(
            onDismiss = { showMemberDialog = false },
            members = members,
            onAddMember = memberViewModel::addMember,
            onDeleteMember = memberViewModel::deleteMember,
            onUpdateMember = { member, name, description ->
                memberViewModel.updateMember(member.copy(name = name, description = description))
            }
        )
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text, modifier = Modifier.padding(start = 8.dp))
    }
}
