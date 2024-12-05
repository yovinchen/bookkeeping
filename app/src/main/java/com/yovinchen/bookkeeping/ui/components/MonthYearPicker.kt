package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth

@Composable
fun MonthYearPicker(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableStateOf(selectedMonth.year) }
    var month by remember { mutableStateOf(selectedMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 年份选择
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("年份:")
                    OutlinedButton(
                        onClick = { year-- }
                    ) {
                        Text("-")
                    }
                    Text(year.toString())
                    OutlinedButton(
                        onClick = { year++ }
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份选择
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("月份:")
                    OutlinedButton(
                        onClick = { 
                            if (month > 1) month--
                        }
                    ) {
                        Text("-")
                    }
                    Text(month.toString())
                    OutlinedButton(
                        onClick = { 
                            if (month < 12) month++
                        }
                    ) {
                        Text("+")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMonthSelected(YearMonth.of(year, month))
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
}
