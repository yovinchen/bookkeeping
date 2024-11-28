package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.viewmodel.MemberDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberName: String,
    yearMonth: YearMonth,
    onNavigateBack: () -> Unit,
    viewModel: MemberDetailViewModel = viewModel()
) {
    val records by viewModel.memberRecords.collectAsState(initial = emptyList())
    val totalAmount by viewModel.totalAmount.collectAsState(initial = 0.0)
    
    LaunchedEffect(memberName, yearMonth) {
        viewModel.loadMemberRecords(memberName, yearMonth)
    }

    val groupedRecords = remember(records) {
        records.groupBy { record ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date)
        }.toSortedMap(reverseOrder())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("$memberName - ${yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 总金额显示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "总支出",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.CHINA)
                            .format(totalAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 按日期分组的记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedRecords.forEach { (date, dayRecords) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(dayRecords.sortedByDescending { it.date }) { record ->
                        RecordItem(record = record)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordItem(record: BookkeepingRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.titleMedium
                )
                if (record.description.isNotBlank()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.CHINA).format(record.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (record.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
