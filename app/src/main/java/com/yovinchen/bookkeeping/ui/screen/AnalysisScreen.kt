package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.MonthYearPicker
import com.yovinchen.bookkeeping.viewmodel.AnalysisType
import com.yovinchen.bookkeeping.viewmodel.AnalysisViewModel
import com.yovinchen.bookkeeping.viewmodel.CategoryStat
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = viewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedType by viewModel.selectedAnalysisType.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 月份选择器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.setSelectedMonth(selectedMonth.minusMonths(1))
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
            }

            Text(
                text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable { showMonthPicker = true }
            )

            IconButton(onClick = {
                viewModel.setSelectedMonth(selectedMonth.plusMonths(1))
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 分析类型选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnalysisType.values().forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { viewModel.setAnalysisType(type) },
                    label = {
                        Text(
                            when (type) {
                                AnalysisType.EXPENSE -> "支出分析"
                                AnalysisType.INCOME -> "收入分析"
                                AnalysisType.TREND -> "收支趋势"
                            }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 统计内容
        when (selectedType) {
            AnalysisType.EXPENSE, AnalysisType.INCOME -> {
                Text(
                    text = if (selectedType == AnalysisType.EXPENSE) "支出分析" else "收入分析",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (categoryStats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 添加饼图
                    CategoryPieChart(
                        categoryData = categoryStats.map { 
                            it.category to it.amount.toFloat()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 分类列表
                LazyColumn {
                    items(categoryStats) { stat ->
                        CategoryStatItem(stat)
                    }
                }
            }
            AnalysisType.TREND -> {
                // TODO: 实现收支趋势图表
                Text(
                    text = "收支趋势",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPicker(
            selectedMonth = selectedMonth,
            onMonthSelected = { yearMonth ->
                viewModel.setSelectedMonth(yearMonth)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
fun CategoryStatItem(stat: CategoryStat) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format("%.1f%%", stat.percentage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { (stat.percentage / 100).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "¥${String.format("%.2f", stat.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${stat.count}笔",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
