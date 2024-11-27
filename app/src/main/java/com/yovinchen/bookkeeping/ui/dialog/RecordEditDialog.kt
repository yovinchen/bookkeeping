@file:OptIn(ExperimentalMaterial3Api::class)

package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.ui.components.DateTimePicker
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun RecordEditDialog(
    record: BookkeepingRecord,
    categories: List<Category>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (BookkeepingRecord) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var amount by remember { mutableStateOf(record.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(record.category) }
    var description by remember { mutableStateOf(record.description) }
    var expanded by remember { mutableStateOf(false) }
    var memberExpanded by remember { mutableStateOf(false) }
    var currentSelectedMember by remember { mutableStateOf<Member?>(null) }
    var selectedDateTime by remember {
        mutableStateOf(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(record.date.time),
                ZoneId.systemDefault()
            )
        )
    }

    // 加载原关联成员
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(record.memberId) {
        if (record.memberId != null) {
            coroutineScope.launch {
                currentSelectedMember = viewModel.getMemberById(record.memberId)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "编辑记录",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 类别选择
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("类别") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.filter { it.type == record.type }.forEach { category ->
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

                Spacer(modifier = Modifier.height(16.dp))

                // 成员选择
                ExposedDropdownMenuBox(
                    expanded = memberExpanded,
                    onExpandedChange = { memberExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentSelectedMember?.name ?: "选择成员",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("成员") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = memberExpanded,
                        onDismissRequest = { memberExpanded = false }
                    ) {
                        // 添加一个"清除选择"选项
                        DropdownMenuItem(
                            text = { Text("清除选择") },
                            onClick = {
                                currentSelectedMember = null
                                memberExpanded = false
                            }
                        )
                        
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    currentSelectedMember = member
                                    memberExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 日期时间选择
                DateTimePicker(
                    selectedDateTime = selectedDateTime,
                    onDateTimeSelected = { selectedDateTime = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 备注输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null) {
                                onConfirm(
                                    record.copy(
                                        amount = amountValue,
                                        category = selectedCategory,
                                        description = description,
                                        date = Date.from(
                                            selectedDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                        ),
                                        memberId = currentSelectedMember?.id
                                    )
                                )
                            }
                        },
                        enabled = amount.isNotEmpty()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
