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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.ui.components.RecordItem
import com.yovinchen.bookkeeping.viewmodel.CategoryDetailViewModel
import com.yovinchen.bookkeeping.viewmodel.CategoryDetailViewModelFactory
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: String,
    month: YearMonth,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { BookkeepingDatabase.getDatabase(context) }
    val viewModel: CategoryDetailViewModel = viewModel(
        factory = CategoryDetailViewModelFactory(database, category, month)
    )

    val records by viewModel.records.collectAsState()
    val total by viewModel.total.collectAsState()
    val members by viewModel.members.collectAsState()
    val groupedRecords = remember(records) {
        records.groupBy { record ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$category - ${month.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "总金额",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%.2f", total),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedRecords.forEach { (date, dayRecords) ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // 日期标签
                                Text(
                                    text = SimpleDateFormat(
                                        "yyyy年MM月dd日 E",
                                        Locale.CHINESE
                                    ).format(dayRecords.first().date),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 当天的记录
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    dayRecords.forEachIndexed { index, record ->
                                        RecordItem(
                                            record = record,
                                            onClick = {},
                                            members = members
                                        )

                                        if (index < dayRecords.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
