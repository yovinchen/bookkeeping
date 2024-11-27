package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yovinchen.bookkeeping.model.TransactionType
import java.time.YearMonth

@Composable
fun MonthYearPickerDialog(
    selectedMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit, onDismiss: () -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(selectedMonth) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择年月",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 年份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.minusYears(1)
                    }) {
                        Text("<")
                    }
                    Text(
                        text = "${currentYearMonth.year}年",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.plusYears(1)
                    }) {
                        Text(">")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = month == currentYearMonth.monthValue

                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1.5f)
                                .clickable {
                                    currentYearMonth = YearMonth.of(currentYearMonth.year, month)
                                },
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${month}月",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 按钮行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onMonthSelected(currentYearMonth)
                        onDismiss()
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyStatistics(
    totalIncome: Double,
    totalExpense: Double,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    selectedType: TransactionType?,
    onClearFilter: () -> Unit,
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 月份选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                }

                Text(text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { showMonthPicker = true })

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 支出统计
                Column(modifier = Modifier
                    .weight(1f)
                    .clickable { onExpenseClick() }
                    .background(
                        if (selectedType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent, RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)) {
                    Text(
                        text = "支出", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalExpense)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // 收入统计
                Column(modifier = Modifier
                    .weight(1f)
                    .clickable { onIncomeClick() }
                    .background(
                        if (selectedType == TransactionType.INCOME) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent, RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)) {
                    Text(
                        text = "收入", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                // 结余统计
                Column(modifier = Modifier
                    .weight(1f)
                    .clickable { onClearFilter() }
                    .background(
                        if (selectedType == TransactionType.INCOME) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent, RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)) {
                    Text(
                        text = "结余", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalIncome - totalExpense)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (totalIncome >= totalExpense) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (selectedType != null) {
                TextButton(
                    onClick = onClearFilter, modifier = Modifier.align(Alignment.End)
                ) {
                    Text("清除筛选")
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onDismiss = { showMonthPicker = false })
    }
}
