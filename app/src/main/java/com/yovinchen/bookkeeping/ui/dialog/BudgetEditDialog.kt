package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.*
import java.util.*

/**
 * 预算编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditDialog(
    budget: Budget? = null,
    categories: List<Category>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    var selectedType by remember { mutableStateOf(budget?.type ?: BudgetType.TOTAL) }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(budget?.categoryName) }
    var selectedMemberId by remember { mutableStateOf(budget?.memberId) }
    var alertThreshold by remember { mutableStateOf((budget?.alertThreshold ?: 0.8) * 100) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (budget == null) "添加预算" else "编辑预算")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 预算类型选择
                Text(
                    text = "预算类型",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == BudgetType.TOTAL,
                        onClick = { selectedType = BudgetType.TOTAL },
                        label = { Text("总预算") }
                    )
                    FilterChip(
                        selected = selectedType == BudgetType.CATEGORY,
                        onClick = { selectedType = BudgetType.CATEGORY },
                        label = { Text("分类预算") }
                    )
                    FilterChip(
                        selected = selectedType == BudgetType.MEMBER,
                        onClick = { selectedType = BudgetType.MEMBER },
                        label = { Text("成员预算") }
                    )
                }
                
                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("预算金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 分类选择（仅在分类预算时显示）
                if (selectedType == BudgetType.CATEGORY) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory ?: "",
                            onValueChange = {},
                            label = { Text("选择分类") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 成员选择（仅在成员预算时显示）
                if (selectedType == BudgetType.MEMBER) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedMember = members.find { it.id == selectedMemberId }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMember?.name ?: "",
                            onValueChange = {},
                            label = { Text("选择成员") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        selectedMemberId = member.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 预警阈值
                Column {
                    Text(
                        text = "预警阈值: ${alertThreshold.toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = alertThreshold.toFloat(),
                        onValueChange = { alertThreshold = it.toDouble() },
                        valueRange = 50f..95f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "当使用金额达到预算的 ${alertThreshold.toInt()}% 时提醒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        val calendar = Calendar.getInstance()
                        val startDate = budget?.startDate ?: calendar.time
                        
                        // 设置结束日期为当月最后一天
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        val endDate = calendar.time
                        
                        val newBudget = Budget(
                            id = budget?.id ?: 0,
                            type = selectedType,
                            amount = amountValue,
                            categoryName = if (selectedType == BudgetType.CATEGORY) selectedCategory else null,
                            memberId = if (selectedType == BudgetType.MEMBER) selectedMemberId else null,
                            startDate = startDate,
                            endDate = endDate,
                            alertThreshold = alertThreshold / 100,
                            isEnabled = budget?.isEnabled ?: true,
                            createdAt = budget?.createdAt ?: Date(),
                            updatedAt = Date()
                        )
                        onConfirm(newBudget)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDouble() > 0 &&
                        (selectedType != BudgetType.CATEGORY || selectedCategory != null) &&
                        (selectedType != BudgetType.MEMBER || selectedMemberId != null)
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
}