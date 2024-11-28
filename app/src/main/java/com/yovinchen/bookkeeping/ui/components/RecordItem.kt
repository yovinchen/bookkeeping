package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordItem(
    record: BookkeepingRecord,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    members: List<Member> = emptyList()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
//    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val member = members.find { it.id == record.memberId }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 第一行：分类
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // 第二行：日期和时间 | 成员 | 详情
                Text(
                    text = buildString {
//                        append(dateFormat.format(record.date))
//                        append(" ")
                        append(timeFormat.format(record.date))
//                        if (member != null && member.name != "自己") {
                            append(" | ")
                        if (member != null) {
                            append(member.name)
                        }
//                        }
                        if (record.description.isNotEmpty()) {
                            append(" | ")
                            append(record.description)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 金额显示
            Text(
                text = String.format("%.2f", record.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (record.type == TransactionType.EXPENSE)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
