package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.data.Record
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.RecordItem
import com.yovinchen.bookkeeping.viewmodel.MemberDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberName: String,
    startMonth: YearMonth,
    endMonth: YearMonth,
    category: String = "",
    analysisType: AnalysisType = AnalysisType.EXPENSE,
    onNavigateBack: () -> Unit,
    viewModel: MemberDetailViewModel = viewModel()
) {
    val records by viewModel.memberRecords.collectAsState(initial = emptyList())
    val totalAmount by viewModel.totalAmount.collectAsState(initial = 0.0)
    val categoryData by viewModel.categoryData.collectAsState(initial = emptyList())

    LaunchedEffect(memberName, category, startMonth, endMonth, analysisType) {
        viewModel.loadMemberRecords(
            memberName = memberName,
            category = category,
            startMonth = startMonth,
            endMonth = endMonth,
            analysisType = analysisType
        )
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
                    Text(memberName) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 第一层：总金额卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "总金额",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.CHINA)
                                .format(totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 当从成员视图进入时显示饼图
            if (category.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "分类统计",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CategoryPieChart(
                                categoryData = categoryData,
                                memberData = emptyList(),
                                currentViewMode = false,
                                onCategoryClick = {
                                    // 暂时不处理点击事件
                                }
                            )
                        }
                    }
                }
            }

            // 第二层：按日期分组的记录列表
            groupedRecords.forEach { (date, dayRecords) ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale.CHINA)
                                        .format(dayRecords.sumOf { it.amount }),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            dayRecords.forEach { record ->
                                RecordItem(record = record)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordItem(record: Record) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            if (record.description.isNotBlank()) {
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(record.dateTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = NumberFormat.getCurrencyInstance(Locale.CHINA)
                .format(record.amount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
