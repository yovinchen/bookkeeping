package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    selectedDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月dd日") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(modifier = modifier) {
        // 日期选择
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Text(
                text = selectedDateTime.format(dateFormatter),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 时间选择
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true }
        ) {
            Text(
                text = selectedDateTime.format(timeFormatter),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    // 自定义日期选择器对话框
    if (showDatePicker) {
        var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDateTime)) }
        
        Dialog(onDismissRequest = { showDatePicker = false }) {
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
                    // 年月选择器
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentYearMonth = currentYearMonth.minusMonths(1)
                        }) {
                            Text("<")
                        }
                        
                        Text(
                            text = "${currentYearMonth.year}年${currentYearMonth.monthValue}月",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(onClick = {
                            currentYearMonth = currentYearMonth.plusMonths(1)
                        }) {
                            Text(">")
                        }
                    }

                    // 星期标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 日期网格
                    val dates = remember(currentYearMonth) {
                        val firstDayOfMonth = currentYearMonth.atDay(1)
                        val lastDayOfMonth = currentYearMonth.atEndOfMonth()
                        val firstDayOfGrid = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong())
                        
                        buildList {
                            var currentDate = firstDayOfGrid
                            while (currentDate.isBefore(lastDayOfMonth.plusDays(7))) {
                                add(currentDate)
                                currentDate = currentDate.plusDays(1)
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(dates.size) { index ->
                            val date = dates[index]
                            val isSelected = date.isEqual(selectedDateTime.toLocalDate())
                            val isCurrentMonth = date.monthValue == currentYearMonth.monthValue
                            
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clickable {
                                        val newDateTime = selectedDateTime
                                            .withYear(date.year)
                                            .withMonth(date.monthValue)
                                            .withDayOfMonth(date.dayOfMonth)
                                        onDateTimeSelected(newDateTime)
                                        showDatePicker = false
                                    },
                                shape = MaterialTheme.shapes.small,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    textAlign = TextAlign.Center,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
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
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showDatePicker = false }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }

    // 时间选择器对话框
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
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
                    val timePickerState = rememberTimePickerState(
                        initialHour = selectedDateTime.hour,
                        initialMinute = selectedDateTime.minute
                    )

                    TimePicker(
                        state = timePickerState
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newDateTime = selectedDateTime
                                    .withHour(timePickerState.hour)
                                    .withMinute(timePickerState.minute)
                                onDateTimeSelected(newDateTime)
                                showTimePicker = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}
