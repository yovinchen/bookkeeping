package com.yovinchen.bookkeeping.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.components.*
import com.yovinchen.bookkeeping.ui.dialog.*
import com.yovinchen.bookkeeping.utils.FilePickerUtil
import com.yovinchen.bookkeeping.viewmodel.*

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
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    
    val categories by viewModel.categories.collectAsState()
    val selectedType by viewModel.selectedCategoryType.collectAsState()
    val members by memberViewModel.allMembers.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // 成员管理设置项
        ListItem(
            headlineContent = { Text("成员管理") },
            supportingContent = { Text("管理账本成员") },
            modifier = Modifier.clickable { showMemberDialog = true }
        )

        HorizontalDivider()

        // 类别管理设置项
        ListItem(
            headlineContent = { Text("类别管理") },
            supportingContent = { Text("管理收入和支出类别") },
            modifier = Modifier.clickable { showCategoryDialog = true }
        )

        HorizontalDivider()

        // 数据备份设置项
        ListItem(
            headlineContent = { Text("数据备份") },
            supportingContent = { Text("备份和恢复数据") },
            modifier = Modifier.clickable { showBackupDialog = true }
        )

        HorizontalDivider()

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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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

        // 备份对话框
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("数据备份") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportToCSV(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("导出为CSV")
                        }
                        Button(
                            onClick = { viewModel.exportToExcel(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("导出为Excel")
                        }
                        Button(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("恢复数据")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("自动备份", modifier = Modifier.weight(1f))
                            Switch(
                                checked = viewModel.isAutoBackupEnabled.collectAsState().value,
                                onCheckedChange = { viewModel.setAutoBackup(it) }
                            )
                        }
                        if (viewModel.isAutoBackupEnabled.collectAsState().value) {
                            Text(
                                "自动备份将每24小时创建一次备份，保存在应用私有目录中",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }

        // 恢复对话框
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("恢复数据") },
                text = {
                    Column {
                        Text("请选择要恢复的备份文件（CSV或Excel格式）")
                        Text(
                            "注意：恢复数据将覆盖当前的所有数据！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRestoreDialog = false
                            // 启动文件选择器
                            val activity = context as? ComponentActivity
                            if (activity != null) {
                                FilePickerUtil.startFilePicker(activity) { file ->
                                    viewModel.restoreData(context, file)
                                }
                            } else {
                                Toast.makeText(context, "无法启动文件选择器", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("选择文件")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 类别管理对话框
        if (showCategoryDialog) {
            CategoryManagementDialog(
                onDismiss = { showCategoryDialog = false },
                categories = categories,
                onAddCategory = { name, type, iconResId -> 
                    viewModel.addCategory(name, type, iconResId)
                },
                onDeleteCategory = viewModel::deleteCategory,
                onUpdateCategory = { category, newName, iconResId ->
                    viewModel.updateCategory(category, newName, iconResId)
                },
                selectedType = selectedType,
                onTypeChange = viewModel::setSelectedCategoryType
            )
        }

        // 成员管理对话框
        if (showMemberDialog) {
            MemberManagementDialog(
                onDismiss = { showMemberDialog = false },
                members = members,
                onAddMember = { name, description, iconResId ->
                    memberViewModel.addMember(name, description, iconResId)
                },
                onDeleteMember = memberViewModel::deleteMember,
                onUpdateMember = { member, name, description, iconResId ->
                    memberViewModel.updateMember(member.copy(
                        name = name,
                        description = description,
                        icon = iconResId
                    ))
                }
            )
        }
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
