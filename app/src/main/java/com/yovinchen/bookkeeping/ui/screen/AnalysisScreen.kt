package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.CategoryStatItem
import com.yovinchen.bookkeeping.ui.components.MonthYearPicker
import com.yovinchen.bookkeeping.viewmodel.AnalysisViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onNavigateToCategoryDetail: (String, YearMonth) -> Unit
) {
    val viewModel: AnalysisViewModel = viewModel()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedAnalysisType by viewModel.selectedAnalysisType.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()

    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份选择器和类型切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 月份选择按钮
                Button(onClick = { showMonthPicker = true }) {
                    Text(selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")))
                }

                // 类型切换
                Row {
                    AnalysisType.values().forEach { type ->
                        FilterChip(
                            selected = selectedAnalysisType == type,
                            onClick = { viewModel.setAnalysisType(type) },
                            label = {
                                Text(
                                    when (type) {
                                        AnalysisType.EXPENSE -> "支出"
                                        AnalysisType.INCOME -> "收入"
                                        AnalysisType.TREND -> "趋势"
                                    }
                                )
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // 使用LazyColumn包含饼图和列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 添加饼图作为第一个项目
                if (selectedAnalysisType != AnalysisType.TREND) {
                    item {
                        CategoryPieChart(
                            categoryData = categoryStats.map { Pair(it.category, it.percentage.toFloat()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            onCategoryClick = { category ->
                                onNavigateToCategoryDetail(category, selectedMonth)
                            }
                        )
                    }
                }

                // 添加分类统计列表项目
                items(categoryStats) { stat ->
                    CategoryStatItem(
                        stat = stat,
                        onClick = { onNavigateToCategoryDetail(stat.category, selectedMonth) }
                    )
                }
            }
        }

        // 月份选择器对话框
        if (showMonthPicker) {
            MonthYearPicker(
                selectedMonth = selectedMonth,
                onMonthSelected = {
                    viewModel.setSelectedMonth(it)
                    showMonthPicker = false
                },
                onDismiss = { showMonthPicker = false }
            )
        }
    }
}
