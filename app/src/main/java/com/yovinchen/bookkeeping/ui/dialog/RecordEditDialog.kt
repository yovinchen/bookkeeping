@file:OptIn(ExperimentalMaterial3Api::class)

package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import java.util.Date

@Composable
fun RecordEditDialog(
    record: BookkeepingRecord,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (BookkeepingRecord) -> Unit
) {
    var amount by remember { mutableStateOf(record.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(record.category) }
    var description by remember { mutableStateOf(record.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑记录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("类别") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { },
                    ) {
                        categories.filter { it.type == record.type }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category.name }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedRecord = record.copy(
                        amount = amount.toDoubleOrNull() ?: record.amount,
                        category = selectedCategory,
                        description = description,
                        date = Date()
                    )
                    onConfirm(updatedRecord)
                    onDismiss()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
