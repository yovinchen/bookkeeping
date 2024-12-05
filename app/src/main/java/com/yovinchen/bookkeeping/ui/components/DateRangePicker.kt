package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    startMonth: YearMonth,
    endMonth: YearMonth,
    onStartMonthSelected: (YearMonth) -> Unit,
    onEndMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartMonthPicker by remember { mutableStateOf(false) }
    var showEndMonthPicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { showStartMonthPicker = true }) {
            Text(startMonth.format(formatter))
        }
        Text("至")
        Button(onClick = { showEndMonthPicker = true }) {
            Text(endMonth.format(formatter))
        }
    }

    if (showStartMonthPicker) {
        MonthYearPicker(
            selectedMonth = startMonth,
            onMonthSelected = {
                onStartMonthSelected(it)
                showStartMonthPicker = false
            },
            onDismiss = { showStartMonthPicker = false }
        )
    }

    if (showEndMonthPicker) {
        MonthYearPicker(
            selectedMonth = endMonth,
            onMonthSelected = {
                onEndMonthSelected(it)
                showEndMonthPicker = false
            },
            onDismiss = { showEndMonthPicker = false }
        )
    }
}
